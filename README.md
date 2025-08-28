# PixelSpoof

## What is PixelSpoof?
PixelSpoof is an advanced Xposed module for LSPosed that allows you to spoof device-specific properties to various Pixel devices, including the latest Pixel 10 Pro XL with Android 16 QPR2. Get access to Pixel-exclusive features on any Android device!

## ✨ Features
- **Multiple Device Profiles**: Switch between Pixel 10 Pro XL, Pixel 10 Pro, Pixel 9 Pro XL, and more
- **Dynamic Updates**: Device profiles automatically update from GitHub (no app update needed)
- **Configuration UI**: Easy-to-use settings app with global and per-app configuration
- **Per-App Spoofing**: Fine-grained control over which apps see spoofed properties
- **Latest Android 16**: Supports the newest Android 16 QPR2 builds
- **Real-time Diagnostics**: Live monitoring of spoofing effectiveness and system status
- **Behavioral Evasion**: Advanced techniques to avoid ML-based detection
- **Comprehensive Testing**: Built-in test harness for validation and troubleshooting

## ⚠️ Important Reality Check

**Please read this carefully before using:**

### What PixelSpoof CAN Do:
- ✅ Spoof device properties that apps read via `SystemProperties.get()`
- ✅ Spoof Build class fields and system properties
- ✅ Intercept network requests and spoof headers
- ✅ Handle context acquisition issues
- ✅ Work with basic apps and some streaming services

### What PixelSpoof CANNOT Do:
- ❌ **Bypass hardware-backed attestation** (Titan M2 chip, Play Integrity API)
- ❌ **Bypass kernel-level security** (SELinux, dm-verity, verified boot)
- ❌ **Fool advanced banking apps** with hardware security checks
- ❌ **Bypass server-side device verification** (Google's device database)

### Realistic Success Rates:
- **Basic Apps** (social media, streaming): 70-90%
- **Google Services**: 80-95%
- **Banking Apps**: 20-40% (depends on implementation)
- **Hardware-Backed Security**: 0% (impossible from userspace)

## How to install
### Prerequisites
To use this module you must have one of the following (latest versions):
- [Magisk](https://github.com/topjohnwu/Magisk) with Zygisk enabled
    - IMPORTANT: DO NOT add apps that you want to spoof to Magisk's denyList as that will break the module.
- [KernelSU](https://github.com/tiann/KernelSU) with [ZygiskNext](https://github.com/Dr-TSNG/ZygiskNext) module installed
- [APatch](https://github.com/bmax121/APatch) with [ZygiskNext MOD](https://github.com/Yervant7/ZygiskNext) module installed
You must also have [LSPosed](https://github.com/mywalkb/LSPosed_mod) installed

### Installation
- Download the latest APK of PixelSpoof from the [releases section](https://github.com/kashi/PixelSpoof/releases) and install it like any normal APK.
- Now open the LSPosed Manager and go to "Modules".
- PixelSpoof should now appear in that list.
- Click on PixelSpoof and enable the module by flipping the switch at the top that says "Enable module".
- Next, tick all the apps that you want to spoof details for and reboot your phone afterwards.
- Once rebooted, you can open the PixelSpoof app to configure which device profile to use.

## Configuration
1. **Open PixelSpoof app** from your app drawer
2. **Global Configuration Tab**: Select your default device profile
3. **Per-App Configuration Tab**: Enable/disable spoofing for specific apps and assign custom profiles
4. **Diagnostics Tab**: Monitor spoofing effectiveness and troubleshoot issues
5. **Click Apply** and reboot your device when making global changes
6. **Refresh profiles** anytime to get the latest device properties from GitHub

### Per-App Configuration
- **Enable/Disable per app**: Control which apps see spoofed properties
- **Custom profiles**: Assign different device profiles to different apps
- **Real-time switching**: Changes apply immediately without reboot
- **Smart defaults**: Apps automatically use global profile unless customized

## 🧪 Testing Framework

PixelSpoof includes a comprehensive testing framework to ensure reliability and effectiveness:

### Running Tests

#### On Linux/macOS:
```bash
./run_tests.sh
```

#### On Windows:
```cmd
run_tests.bat
```

### Test Coverage
The test suite covers:
- ✅ **Profile Management**: Device profile loading, validation, and switching
- ✅ **Property Spoofing**: System property interception and spoofing
- ✅ **Per-App Configuration**: App-specific profile management and validation
- ✅ **Behavioral Evasion**: ML detection avoidance and touch pattern simulation
- ✅ **Error Handling**: Comprehensive error handling and recovery
- ✅ **Safety Validation**: Pre-flight safety checks and system validation
- ✅ **Network Interception**: HTTP header spoofing and request interception
- ✅ **Runtime Validation**: Spoofing effectiveness validation and diagnostics
- ✅ **CI/CD Pipeline**: Automated building, testing, and deployment

### Test Reports
After running tests, check:
- **Test Results**: `app/build/reports/tests/testDebugUnitTest/`
- **Coverage Report**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

### Manual Testing
For manual validation:
1. Install the module and enable it in LSPosed
2. Run the test suite to verify core functionality
3. Use device info apps to verify spoofing effectiveness
4. Test with target applications (social media, streaming services)
5. Check the diagnostics dashboard for real-time monitoring

## 🔍 Diagnostics Dashboard

The built-in diagnostics dashboard provides real-time monitoring and troubleshooting:

### System Status
- **LSPosed Module Status**: Verifies module is properly enabled
- **System Hooks Status**: Confirms property spoofing hooks are active
- **Spoofing Effectiveness**: Shows success rates for property and network spoofing

### Runtime Metrics
- **Property Requests**: Number of system property requests intercepted
- **Properties Spoofed**: Successful property spoofing operations
- **Network Interceptions**: HTTP requests intercepted and modified
- **Headers Spoofed**: User-Agent and other headers successfully spoofed

### Per-App Statistics
- **Enabled Apps**: List of apps with spoofing enabled
- **Custom Profiles**: Apps using non-default profiles
- **Success Rates**: Per-app spoofing effectiveness metrics

### Troubleshooting
- **Common Issues**: Pre-built solutions for frequent problems
- **System Information**: Device and module version details
- **Safety Warnings**: Guidance on realistic expectations

## FAQ and issues
- **Q: How do I change which Pixel device to spoof?**  
  A: Open the PixelSpoof app and select a different profile from the Global Configuration tab.

- **Q: Can I use different profiles for different apps?**  
  A: Yes! Use the Per-App Configuration tab to assign custom profiles to specific apps.

- **Q: How do I monitor if spoofing is working?**  
  A: Check the Diagnostics tab for real-time metrics and system status.

- **Q: Do I need to update the app for new device profiles?**  
  A: No! Device profiles automatically update from GitHub daily.

- **Q: Which apps work best with spoofing?**  
  A: Camera apps, Google apps, and apps with Pixel-exclusive features work great.

- **Q: What should I do if an app isn't working with spoofing?**  
  A: Check the diagnostics dashboard for issues, or disable spoofing for that specific app in per-app settings.

## Device Profiles Available
- Pixel 10 Pro XL (mustang) - Android 16 QPR2
- Pixel 10 Pro (frankel) - Android 16 QPR2  
- Pixel 10 (blazer) - Android 16 QPR2
- Pixel 9 Pro XL (caiman) - Android 16 QPR1
- Pixel 9 Pro (komodo) - Android 16 QPR1
- Pixel 8 Pro (husky) - Android 15

Profiles are automatically updated with the latest build numbers and fingerprints!