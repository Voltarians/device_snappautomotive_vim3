# Promethean Core Android Auto + Factory Radio DSP Integration

## Baseline

This branch starts from the accepted HMI v1 checkpoint:

- commit: `1def32ec8baea815badd7ebe382f40d84d96514d`
- checkpoint branch: `promethean-core-hmi-v1-checkpoint`
- AAOS baseline remains unchanged:
  - VIM3
  - Android Automotive 15
  - kernel 5.15.74
  - Ethernet 192.168.137.3
  - persistent Ethernet ADB on TCP 5555
  - USB-free cold boot

Do not regress the accepted Ethernet/ADB baseline while integrating projection.

## Android Auto receiver selection

Initial receiver candidate: **OpenAutoLink**.

Reasons for the first hardware trial:

- built specifically for Android Automotive OS
- supports Android Auto projection on AAOS systems that permit APK sideloading
- supports wireless and USB transports
- current wireless path targets Android Auto 17.4+ using WPP
- supports ignition reconnect, calls/microphone, steering-wheel controls and vehicle-data integration
- car application package: `com.openautolink.app`
- main activity: `.MainActivity`

Initial validation release selected on 2026-09-23:

- OpenAutoLink v0.1.504
- car APK: `openautolink-car-v0.1.504.apk`
- companion APK: `openautolink-companion-v0.1.504.apk`
- car APK SHA-256: `c9b1b19b25d10cb3dd2e76f5ba7575dd4134bd80e97bd6d69cbb36cce76f8000`
- companion SHA-256: `0608fc713fe0f0c8d11d34d4ed44fc23832afd5cee2922e73b5a87af8dad678d`

## Validation sequence

1. Sideload the car APK onto the VIM3 without modifying the system image.
2. Launch `com.openautolink.app/.MainActivity`.
3. Confirm landscape rendering and touch mapping.
4. Confirm no regression to Promethean HMI, Ethernet, or ADB.
5. Install the matching companion APK on the Android phone.
6. Validate USB projection first if wireless setup is not immediately available.
7. Validate wireless WPP projection.
8. Validate reconnect after phone leaves and returns.
9. Only after stable projection, add Android Auto launch/auto-launch controls to Promethean HMI.
10. Keep an OFF / ASK / AUTOMATIC user setting for automatic projection launch.

## Audio architecture requirement

Android Auto integration must not bypass the desired factory-radio DSP path.

Target architecture:

```
Android phone
    |
Android Auto projection
    |
VIM3 / Promethean Core
    |
audio output / validated injection interface
    |
factory Volt radio source/input stage
    |
factory radio DSP
    |
factory output stage or later external amplifier
    |
speakers
```

The first audio test may use a quality USB DAC/audio interface into an available factory-radio source input because it is reversible and establishes the end-to-end path.

The long-term hardware task is to identify the cleanest injection point **upstream of the factory DSP**, not merely the signal path between DSP and power amplifier.

## Factory radio DSP bench work

Parallel hardware work:

1. Document the exact 2013 Volt base-radio board revision.
2. Trace source-audio paths into the NXP audio/DSP section.
3. Identify candidate analog or digital source inputs before DSP processing.
4. Scope idle and active audio signals at each candidate.
5. Determine signal format, level, bias and sample rate where applicable.
6. Inject a low-level known test tone at a reversible point.
7. Verify that factory volume, EQ, balance and fade still affect the injected signal.
8. Verify factory audio remains functional when Promethean Core is disconnected.
9. Only after bench validation design a permanent isolated interface.

## Acceptance criteria

Android Auto software acceptance:

- projection works on the VIM3
- touch mapping is correct
- audio is stable
- microphone/call path is testable
- reconnect works
- Promethean HMI remains available
- Ethernet ADB remains available

Factory DSP acceptance:

- Promethean audio enters upstream of DSP
- factory DSP functions remain effective
- no noise, clipping, DC offset or boot transient is introduced
- factory fallback remains available
