# Promethean Core HMI foundation

This branch begins the Promethean Core human-machine interface on top of the accepted VIM3 AAOS hardware baseline.

## Baseline contract

The HMI work must not regress the accepted VIM3 platform:

- Linux kernel 5.15.74
- static Ethernet service address 192.168.137.3
- persistent development ADB on TCP/5555 for userdebug/eng builds
- USB-free cold boot
- scrcpy over Ethernet
- the accepted 3 GiB super-partition change in the Voltarians Yukawa fork

The HMI branch is based directly on promethean-vim3-ethernet.

## Milestone 1

PrometheanCoreHMI is an in-tree AAOS application with no external AndroidX or Compose dependency. It is included in the VIM3 product image but is not yet registered as the HOME application.

That sequencing is intentional. The first acceptance run is:

1. build the VIM3 AAOS image;
2. boot normally with no USB dependency;
3. connect with ADB at 192.168.137.3:5555;
4. start the HMI with: adb shell am start -n com.promethean.core.hmi/.MainActivity
5. verify rendering and touch at the target display resolution;
6. verify Ethernet ADB and scrcpy still work after cold boot;
7. only after that acceptance gate, promote the HMI into the launcher/HOME role.

## Interface direction

The first screen is deliberately Volt-inspired rather than a pixel copy. It establishes:

- a central speed/gear focal point;
- electric range, battery state and propulsion state;
- traction-power presentation;
- climate quick-view space;
- persistent Energy, Climate, Audio, Vehicle and Apps surfaces;
- a canonical data boundary between the HMI and vehicle-specific transports.

## Vehicle-data architecture

The UI does not talk directly to CAN, SWCAN, OBD adapters, or a particular generation of Volt.

Intended data path:

Gen-1 / Gen-2 vehicle sources -> Voltarian / PCG-1 / AAOS adapters -> canonical Promethean vehicle state -> HMI

The initial VehicleDataSource is a clearly labeled demo provider and always reports zero road speed. The next implementation step is a process-safe Promethean vehicle-state service with adapters for Voltarian, PCG-1 Ethernet, AAOS CarProperty where appropriate, and replay/test captures.

The canonical model will allow one HMI to render both Gen-1 and Gen-2 Volt data without duplicating the interface.

## Next acceptance target

After the screen builds and renders on the VIM3, replace the demo provider with the canonical vehicle-state service while retaining an explicit simulation/replay mode for bench work.
