# Promethean Core VIM3 AAOS Ethernet Baseline

Status: **ACCEPTED**
Acceptance date: 2026-09-22

This document records the known-good Ethernet baseline for Promethean Core on the Khadas VIM3 Pro running Snapp Automotive AAOS 15. It captures the source changes, build inputs, partition geometry, flash procedure, and acceptance checks that were proven on hardware.

## Known-good platform

- Board: Khadas VIM3 Pro
- Android target: `snapp_car_vim3-ap3a-userdebug`
- Android build ID observed during build: `APM1.240918.007`
- Kernel: `5.15.74-gbd68ce64e647-ab9273711`
- Device-tree model after boot: `Khadas VIM3`
- Promethean Core Ethernet address: `192.168.137.3/24`
- Intended local backbone:
  - Windows/service host: `192.168.137.1`
  - PCG-1: `192.168.137.2`
  - VIM3/Promethean Core: `192.168.137.3`

Wi-Fi remains available for the normal/default route. The Promethean Ethernet interface intentionally has no gateway or DNS configuration.

## Accepted source commits

This branch contains:

- `d4689a6` — Add persistent Promethean VIM3 Ethernet configuration

The companion Yukawa fork/branch contains:

- Repository: `Voltarians/device_amlogic_yukawa`
- Branch: `promethean-vim3-super-3g`
- Commit: `eb21bf8` — Match VIM3 super geometry to physical 3 GiB partition

Both changes are required for the fully tested baseline.

## Persistent Connectivity RRO

The VIM3 product includes a product-specific runtime resource overlay:

`overlay_packages/PrometheanConnectivityOverlay/`

Files:

- `Android.bp`
- `AndroidManifest.xml`
- `res/values/config.xml`

The product makefile includes:

```make
CarConnectivityOverlay \
PrometheanConnectivityOverlay \
CarWifiOverlay \
```

The accepted Ethernet resource is:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string-array translatable="false" name="config_ethernet_interfaces">
        <item>eth0;11,12,13,14,15,28,37;ip=192.168.137.3/24;</item>
    </string-array>
</resources>
```

The capability set was required so Android's Ethernet network offer satisfies standing framework requests and creates an active `IpClient` / `NetworkAgent`.

Accepted capability IDs:

- 11 — NOT_METERED
- 12 — INTERNET
- 13 — NOT_RESTRICTED
- 14 — TRUSTED
- 15 — NOT_VPN
- 28 — NOT_VCN_MANAGED
- 37 — NOT_BANDWIDTH_CONSTRAINED

## Build environment

Build host used for acceptance:

- Windows 11 Pro host: `Exec-OTG`
- WSL: Ubuntu 24.04
- AAOS tree: `~/promethean-core-aaos`
- Build parallelism used: `-j2` on a machine with about 11.7 GB RAM

Set up the build environment:

```bash
cd ~/promethean-core-aaos

source build/envsetup.sh
export TARGET_KERNEL_USE=5.15
lunch snapp_car_vim3-ap3a-userdebug
```

Build the system image:

```bash
m systemimage -j2
```

Build the complete dynamic-partition super image after the 3 GiB Yukawa geometry override is present:

```bash
m superimage -j2
```

## Known-good image hashes

Accepted build artifacts:

```text
system.img
00df15e7758ed7e25ed08778093184bb681810fdf9d433971719874c2c649db7

PrometheanConnectivityOverlay.apk
35dd228c545c2680d119ac22401bf18b18b6694e82cf4addcec3947137146c09

3 GiB super.img
75949c296255c9494747abb236a1097e3ed5d48aaf089a6789b36e325b662ce4
```

Verify the overlay is physically contained in `system.img`:

```bash
debugfs -R \
  'stat /product/overlay/PrometheanConnectivityOverlay.apk' \
  out/target/product/yukawa/system.img

rm -f /tmp/PrometheanConnectivityOverlay-from-system.apk

debugfs -R \
  'dump -p /product/overlay/PrometheanConnectivityOverlay.apk /tmp/PrometheanConnectivityOverlay-from-system.apk' \
  out/target/product/yukawa/system.img

sha256sum /tmp/PrometheanConnectivityOverlay-from-system.apk
```

Expected APK hash:

```text
35dd228c545c2680d119ac22401bf18b18b6694e82cf4addcec3947137146c09
```

## Super-partition geometry requirement

The physical VIM3 `super` partition is exactly:

```text
3221225472 bytes
0xC0000000
3 GiB
```

The accepted Promethean build must produce LP metadata with:

```text
Block device size:      3221225472
Dynamic group maximum:  3217031168
```

Verify the built image:

```bash
rm -f /tmp/yukawa-super-3g.raw.img

out/host/linux-x86/bin/simg2img \
  out/target/product/yukawa/super.img \
  /tmp/yukawa-super-3g.raw.img

stat -c '%s bytes' /tmp/yukawa-super-3g.raw.img

out/host/linux-x86/bin/lpdump \
  /tmp/yukawa-super-3g.raw.img 2>&1 | head -160
```

Do not use a `super.img` whose LP block-device size does not match the physical 3 GiB partition.

## Why the complete super image is flashed

An earlier attempt to flash only the logical `system` partition in fastbootd failed at the automatic resize step:

```text
Resizing 'system' FAILED (remote: 'Failed to write partition table')
```

The image itself fit inside the existing logical system partition. The failure was caused by inconsistent dynamic-partition geometry:

- Physical VIM3 `super`: `3221225472` bytes
- Previously installed LP metadata described `super` as `2415919104` bytes
- Original build configuration expected `2625634304` bytes

The accepted solution is to build a complete `super.img` with metadata matching the actual 3 GiB physical partition and flash the physical `super` partition from bootloader fastboot.

## Pre-flash backup

Before replacing `super`, keep a complete copy of the currently installed physical partition.

From Windows PowerShell:

```powershell
cd C:\Users\Monroe\AndroidTools\platform-tools

.\adb.exe root
.\adb.exe wait-for-device
.\adb.exe shell "df -h /data"
```

If enough free space is available:

```powershell
.\adb.exe shell "dd if=/dev/block/by-name/super of=/data/local/tmp/vim3-super-before-promethean.img bs=4M"

.\adb.exe pull /data/local/tmp/vim3-super-before-promethean.img .\vim3-super-before-promethean.img

(Get-Item .\vim3-super-before-promethean.img).Length
```

Expected length:

```text
3221225472
```

After confirming the host copy:

```powershell
.\adb.exe shell "rm /data/local/tmp/vim3-super-before-promethean.img"
```

Keep the host backup in a safe location.

## Flash procedure

Copy the accepted sparse `super.img` from WSL to Windows:

```powershell
wsl.exe -d Ubuntu-24.04 -- cp /home/monroe/promethean-core-aaos/out/target/product/yukawa/super.img /mnt/c/Users/Monroe/AndroidTools/platform-tools/vim3-super-promethean.img
```

Verify the image hash:

```powershell
Get-FileHash .\vim3-super-promethean.img -Algorithm SHA256
```

Expected:

```text
75949C296255C9494747ABB236A1097E3ED5D48AAF089A6789B36E325B662CE4
```

Enter **bootloader fastboot**, not fastbootd:

```powershell
.\adb.exe reboot bootloader
Start-Sleep -Seconds 8

.\fastboot.exe getvar is-userspace
.\fastboot.exe getvar partition-size:super
```

Expected:

```text
is-userspace: no
partition-size:super: 0x00000000c0000000
```

Flash only the physical `super` partition:

```powershell
.\fastboot.exe flash super .\vim3-super-promethean.img
```

The accepted hardware flash completed as 17 sparse chunks with every send/write operation returning `OKAY`.

Reboot:

```powershell
.\fastboot.exe reboot
.\adb.exe wait-for-device
```

The existing known-good 5.15 `boot` partition is not modified by this `super` flash.

## Post-flash acceptance checks

Verify kernel and board:

```powershell
.\adb.exe shell uname -a
.\adb.exe shell "cat /proc/device-tree/model; echo"
```

Accepted values include:

```text
Linux localhost 5.15.74-gbd68ce64e647-ab9273711 ...
Khadas VIM3
```

Verify the overlay:

```powershell
.\adb.exe shell "cmd overlay list | grep -i -E 'connectivity|promethean'"

.\adb.exe shell "cmd overlay lookup --verbose com.android.connectivity.resources com.android.connectivity.resources:array/config_ethernet_interfaces"
```

Expected resolved resource:

```text
eth0;11,12,13,14,15,28,37;ip=192.168.137.3/24;
```

Verify network configuration:

```powershell
.\adb.exe shell ip addr show eth0
.\adb.exe shell ip route
```

Expected essentials:

```text
eth0: ... UP,LOWER_UP
inet 192.168.137.3/24
192.168.137.0/24 dev eth0 ... src 192.168.137.3
```

Verify LP metadata:

```powershell
.\adb.exe shell "lpdump 2>&1 | head -80"
```

Expected:

```text
Partition name: super
Size: 3221225472 bytes

Name: db_dynamic_partitions
Maximum size: 3217031168 bytes
```

Verify host reachability:

```powershell
ping -n 6 192.168.137.3
```

Accepted cold-start test result:

```text
Packets: Sent = 6, Received = 6, Lost = 0 (0% loss)
Round-trip: 0-1 ms
```

## Cold-power acceptance test

For final hardware acceptance:

1. Shut Android down cleanly:

   ```powershell
   .\adb.exe shell reboot -p
   ```

2. Remove VIM3 power completely for about 20 seconds.
3. Restore power.
4. Wait for ADB:

   ```powershell
   .\adb.exe wait-for-device
   ```

5. Repeat the kernel, board, overlay, IP, route, LP metadata, and ping checks above.

The accepted Promethean Core baseline passed this cold-power test with the static Ethernet configuration returning automatically and 6/6 host pings successful.

## Acceptance definition

The VIM3 Ethernet baseline is accepted only when all of the following are true after a full power loss:

- Kernel remains 5.15.74.
- Device-tree model is Khadas VIM3.
- `PrometheanConnectivityOverlay.apk` is loaded from the system image.
- The overlay resolves `eth0` to `192.168.137.3/24` with the accepted capability list.
- `eth0` is `UP,LOWER_UP`.
- The local backbone route exists.
- LP metadata describes the physical 3 GiB super partition correctly.
- Windows can ping `192.168.137.3` reliably.
- No `adb remount`, manual APK push, or manual `ip addr add` is required.

When all conditions pass, the baseline is considered **Promethean Core VIM3 AAOS Ethernet — ACCEPTED**.
