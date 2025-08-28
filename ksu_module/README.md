# Pixel 10 Pro XL KernelSU Spoofing Module

This KernelSU module modifies system properties to make your device appear as a Google Pixel 10 Pro XL with Tensor G5 processor running Android 15.

## Features
- ✅ Complete system-level property spoofing
- ✅ Hardware CPU/SOC information spoofing (/proc/cpuinfo)
- ✅ Google Play Store certification fix
- ✅ Pixel exclusive features enabled
- ✅ Android 15 (API 35) spoofing
- ✅ Tensor G5 hardware spoofing
- ✅ Works with KSU-Next + SusFS

## Installation
1. Copy this module folder to your device
2. Install via KernelSU Manager
3. Reboot device
4. Check `/data/local/tmp/pixel_spoof.log` for status

## What gets spoofed
- Device manufacturer: Google
- Device model: Pixel 10 Pro XL  
- Device codename: mustang
- Android version: 15 (API 35)
- SoC: Google Tensor G5
- Build fingerprint: google/mustang/mustang:15/BP2A.250805.005/2025082800:user/release-keys
- Security patch: 2025-08-05
- CPU info: Spoofed to show Google Tensor G5 cores

## Verification
After reboot, check:
```bash
getprop ro.product.manufacturer  # Should show "Google"
getprop ro.product.model         # Should show "Pixel 10 Pro XL"
getprop ro.build.fingerprint     # Should show Pixel fingerprint
getprop ro.soc.model            # Should show "Tensor G5"
cat /proc/cpuinfo               # Should show Google CPU cores
```

## Compatibility
- Requires KernelSU with resetprop support
- Works best with KSU-Next + SusFS
- Compatible with Android 11+

## Safety
This module only modifies cosmetic properties and does not affect:
- Bootloader security
- System integrity
- Hardware functionality
- Root detection bypasses
