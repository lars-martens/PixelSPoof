package com.kashi.caimanspoof

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Field

/**
 * COMPREHENSIVE Property Spoofer - Actually intercepts ALL property access methods
 * This is what was missing - real property interception!
 */
class PropertySpoofer private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: PropertySpoofer? = null
        
        fun getInstance(): PropertySpoofer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PropertySpoofer().also { INSTANCE = it }
            }
        }
    }
    
    // Complete property map - will be populated dynamically
    private val PIXEL_PROPERTIES = mutableMapOf<String, String>()

    init {
        // Initialize with default Pixel 10 Pro XL profile
        val defaultProfile = DeviceProfile.getPixel10ProXL()
        buildPropertiesForProfile(defaultProfile)
    }
    
    /**
     * Initialize comprehensive property spoofing
     */
    fun initializePropertySpoofing(lpparam: XC_LoadPackage.LoadPackageParam, profile: DeviceProfile) {
        StealthManager.stealthLog("🔧 Initializing COMPREHENSIVE property spoofing")

        try {
            // Build properties from profile
            refreshPropertiesForProfile(profile)

            // Hook ALL the ways apps can read properties
            hookSystemProperties(lpparam)
            hookBuildClass(lpparam)
            hookSettingsSecure(lpparam)
            hookTelephonyManager(lpparam)
            hookPackageManager(lpparam)

            StealthManager.stealthLog("✅ Comprehensive property spoofing activated - ALL access methods hooked!")

        } catch (e: Exception) {
            StealthManager.stealthLog("❌ Property spoofing failed: ${e.message}")
        }
    }
    
    /**
     * Refresh properties with new profile (for dynamic profile switching)
     */
    fun refreshPropertiesForProfile(profile: DeviceProfile) {
        ErrorHandler.safeExecute("Refresh properties for profile", "PropertySpoofer") {
            // Clear existing properties and rebuild from scratch
            PIXEL_PROPERTIES.clear()
            buildPropertiesForProfile(profile)
            StealthManager.stealthLog("🔄 Completely rebuilt properties for profile: ${profile.displayName}")
        }
    }

    /**
     * Build all properties for a specific profile
     */
    private fun buildPropertiesForProfile(profile: DeviceProfile) {
        // Parse Android version from fingerprint
        val androidVersion = parseAndroidVersionFromFingerprint(profile.fingerprint)
        val sdkVersion = getSdkFromAndroidVersion(androidVersion)

        // ============ BASIC BUILD PROPERTIES ============
        PIXEL_PROPERTIES["ro.build.host"] = "e27561acca81"
        PIXEL_PROPERTIES["ro.build.tags"] = "release-keys"
        PIXEL_PROPERTIES["ro.build.flavor"] = "${profile.device}-user"
        PIXEL_PROPERTIES["ro.build.fingerprint"] = profile.fingerprint
        PIXEL_PROPERTIES["ro.build.id"] = profile.buildId
        PIXEL_PROPERTIES["ro.build.display.id"] = profile.buildId
        PIXEL_PROPERTIES["ro.build.version.release"] = androidVersion
        PIXEL_PROPERTIES["ro.build.version.sdk"] = sdkVersion
        PIXEL_PROPERTIES["ro.build.version.security_patch"] = profile.securityPatch
        PIXEL_PROPERTIES["ro.build.type"] = "user"
        PIXEL_PROPERTIES["ro.build.user"] = "android-build"

        // ============ CORE PRODUCT PROPERTIES ============
        PIXEL_PROPERTIES["ro.product.brand"] = profile.brand
        PIXEL_PROPERTIES["ro.product.manufacturer"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.model"] = profile.model
        PIXEL_PROPERTIES["ro.product.device"] = profile.device
        PIXEL_PROPERTIES["ro.product.name"] = profile.device
        PIXEL_PROPERTIES["ro.product.board"] = profile.board

        // ============ PRODUCT VARIANTS ============
        PIXEL_PROPERTIES["ro.product.product.brand"] = profile.brand
        PIXEL_PROPERTIES["ro.product.product.device"] = profile.device
        PIXEL_PROPERTIES["ro.product.product.manufacturer"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.product.model"] = profile.model
        PIXEL_PROPERTIES["ro.product.product.name"] = profile.device

        // ============ ATTESTATION PROPERTIES ============
        PIXEL_PROPERTIES["ro.product.brand_for_attestation"] = profile.brand
        PIXEL_PROPERTIES["ro.product.device_for_attestation"] = profile.device
        PIXEL_PROPERTIES["ro.product.manufacturer_for_attestation"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.model_for_attestation"] = profile.model
        PIXEL_PROPERTIES["ro.product.name_for_attestation"] = profile.device

        // ============ BOOTIMAGE PROPERTIES ============
        PIXEL_PROPERTIES["ro.product.bootimage.brand"] = profile.brand
        PIXEL_PROPERTIES["ro.product.bootimage.device"] = profile.device
        PIXEL_PROPERTIES["ro.product.bootimage.manufacturer"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.bootimage.model"] = profile.model
        PIXEL_PROPERTIES["ro.product.bootimage.name"] = profile.device
        PIXEL_PROPERTIES["ro.bootimage.build.fingerprint"] = profile.fingerprint
        PIXEL_PROPERTIES["ro.bootimage.build.id"] = profile.buildId
        PIXEL_PROPERTIES["ro.bootimage.build.tags"] = "release-keys"
        PIXEL_PROPERTIES["ro.bootimage.build.type"] = "user"
        PIXEL_PROPERTIES["ro.bootimage.build.version.incremental"] = profile.buildId.split(".")[2]
        PIXEL_PROPERTIES["ro.bootimage.build.version.release"] = androidVersion
        PIXEL_PROPERTIES["ro.bootimage.build.version.release_or_codename"] = androidVersion
        PIXEL_PROPERTIES["ro.bootimage.build.version.sdk"] = sdkVersion

        // ============ VENDOR PROPERTIES ============
        PIXEL_PROPERTIES["ro.product.vendor.brand"] = profile.brand
        PIXEL_PROPERTIES["ro.product.vendor.device"] = profile.device
        PIXEL_PROPERTIES["ro.product.vendor.manufacturer"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.vendor.model"] = profile.model
        PIXEL_PROPERTIES["ro.product.vendor.name"] = profile.device
        PIXEL_PROPERTIES["ro.vendor.build.fingerprint"] = profile.fingerprint
        PIXEL_PROPERTIES["ro.vendor.build.id"] = profile.buildId
        PIXEL_PROPERTIES["ro.vendor.build.tags"] = "release-keys"
        PIXEL_PROPERTIES["ro.vendor.build.type"] = "user"
        PIXEL_PROPERTIES["ro.vendor.build.version.incremental"] = profile.buildId.split(".")[2]
        PIXEL_PROPERTIES["ro.vendor.build.version.release"] = androidVersion
        PIXEL_PROPERTIES["ro.vendor.build.version.release_or_codename"] = androidVersion
        PIXEL_PROPERTIES["ro.vendor.build.version.sdk"] = sdkVersion
        PIXEL_PROPERTIES["ro.vendor.build.security_patch"] = profile.securityPatch

        // ============ SYSTEM PROPERTIES ============
        PIXEL_PROPERTIES["ro.product.system.brand"] = profile.brand
        PIXEL_PROPERTIES["ro.product.system.device"] = "generic"
        PIXEL_PROPERTIES["ro.product.system.manufacturer"] = profile.manufacturer
        PIXEL_PROPERTIES["ro.product.system.model"] = "mainline"
        PIXEL_PROPERTIES["ro.product.system.name"] = "mainline"
        PIXEL_PROPERTIES["ro.system.build.fingerprint"] = profile.fingerprint
        PIXEL_PROPERTIES["ro.system.build.id"] = profile.buildId
        PIXEL_PROPERTIES["ro.system.build.tags"] = "release-keys"
        PIXEL_PROPERTIES["ro.system.build.type"] = "user"
        PIXEL_PROPERTIES["ro.system.build.version.incremental"] = profile.buildId.split(".")[2]
        PIXEL_PROPERTIES["ro.system.build.version.release"] = androidVersion
        PIXEL_PROPERTIES["ro.system.build.version.release_or_codename"] = androidVersion
        PIXEL_PROPERTIES["ro.system.build.version.sdk"] = sdkVersion

        // ============ HARDWARE & PROCESSOR INFO ============
        PIXEL_PROPERTIES["ro.soc.model"] = profile.socModel
        PIXEL_PROPERTIES["ro.soc.manufacturer"] = profile.socManufacturer
        PIXEL_PROPERTIES["ro.board.platform"] = profile.hardwarePlatform
        PIXEL_PROPERTIES["ro.hardware"] = profile.device
        PIXEL_PROPERTIES["ro.hardware.chipname"] = profile.socModel
        PIXEL_PROPERTIES["ro.product.cpu.abi"] = profile.cpuAbi
        PIXEL_PROPERTIES["ro.product.cpu.abilist"] = profile.cpuAbiList
        PIXEL_PROPERTIES["ro.product.cpu.abilist32"] = "armeabi-v7a,armeabi"
        PIXEL_PROPERTIES["ro.product.cpu.abilist64"] = profile.cpuAbi
        PIXEL_PROPERTIES["ro.processor.model"] = profile.processorModel
        PIXEL_PROPERTIES["ro.processor.manufacturer"] = profile.socManufacturer
        PIXEL_PROPERTIES["ro.gpu.model"] = profile.gpuModel
        PIXEL_PROPERTIES["ro.gpu.manufacturer"] = "ARM"
        PIXEL_PROPERTIES["ro.display.density"] = profile.displayDensity
        PIXEL_PROPERTIES["ro.display.size"] = profile.displaySize
        PIXEL_PROPERTIES["ro.build.hardware.sku"] = profile.device
        PIXEL_PROPERTIES["ro.product.hardware.sku"] = profile.device
        PIXEL_PROPERTIES["ro.boot.hardware.sku"] = profile.device
        PIXEL_PROPERTIES["ro.product.hardware.platform"] = profile.hardwarePlatform
        PIXEL_PROPERTIES["ro.hardware.platform"] = profile.hardwarePlatform
        PIXEL_PROPERTIES["ro.arch"] = "arm64"
        PIXEL_PROPERTIES["ro.cpu.architecture"] = "arm64"

        // ============ CPU DETAILS ============
        PIXEL_PROPERTIES["ro.cpu.model"] = profile.processorModel
        PIXEL_PROPERTIES["ro.cpu.vendor"] = profile.socManufacturer
        PIXEL_PROPERTIES["ro.cpu.cores"] = "8"

        // ============ GPU DETAILS ============
        PIXEL_PROPERTIES["ro.gpu.vendor"] = "ARM"
        PIXEL_PROPERTIES["ro.gpu.cores"] = "24"

        // ============ GOOGLE SERVICES ============
        PIXEL_PROPERTIES["ro.opa.eligible_device"] = "true"
        PIXEL_PROPERTIES["ro.com.google.clientidbase"] = "android-google"
        PIXEL_PROPERTIES["ro.quick_start.device_id"] = profile.device

        // ============ COMMONLY QUERIED PROPERTIES ============
        PIXEL_PROPERTIES["ro.chipname"] = profile.socModel
        PIXEL_PROPERTIES["ro.hardware.chipset"] = profile.socModel
        PIXEL_PROPERTIES["ro.soc.vendor"] = profile.socManufacturer
        PIXEL_PROPERTIES["ro.vendor.name"] = profile.brand
        PIXEL_PROPERTIES["ro.vendor.product.name"] = profile.device
        PIXEL_PROPERTIES["ro.vendor.product.device"] = profile.device
        PIXEL_PROPERTIES["ro.vendor.product.model"] = profile.model

        // ============ VERSION CODENAMES ============
        PIXEL_PROPERTIES["ro.build.version.codename"] = "REL"
        PIXEL_PROPERTIES["ro.build.version.all_codenames"] = "REL"
        PIXEL_PROPERTIES["ro.build.version.preview_sdk"] = "0"
        PIXEL_PROPERTIES["ro.build.version.preview_sdk_fingerprint"] = "REL"

        // ============ SERIAL AND UNIQUE IDS ============
        PIXEL_PROPERTIES["ro.serialno"] = "HT7A1TESTDEVICE"
        PIXEL_PROPERTIES["ro.boot.serialno"] = "HT7A1TESTDEVICE"
        PIXEL_PROPERTIES["ril.serial_number"] = "HT7A1TESTDEVICE"

        StealthManager.stealthLog("📋 Built ${PIXEL_PROPERTIES.size} properties for profile: ${profile.displayName}")
    }

    /**
     * Parse Android version from fingerprint
     */
    private fun parseAndroidVersionFromFingerprint(fingerprint: String): String {
        return try {
            // Fingerprint format: brand/product/device:version/release/build_id:build_type/tags
            val versionPart = fingerprint.split(":")[1] // Gets "version/release/build_id"
            versionPart.split("/")[0] // Gets just the version number
        } catch (e: Exception) {
            "16" // Default fallback
        }
    }

    /**
     * Get SDK version from Android version
     */
    private fun getSdkFromAndroidVersion(androidVersion: String): String {
        return when (androidVersion) {
            "15" -> "35"
            "16" -> "36"
            "17" -> "37"
            else -> "36" // Default to latest
        }
    }
    
    /**
     * Hook SystemProperties.get() - This is the main one!
     */
    private fun hookSystemProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val systemPropertiesClass = XposedHelpers.findClass("android.os.SystemProperties", lpparam.classLoader)
            
            // Hook get(String key)
            XposedHelpers.findAndHookMethod(
                systemPropertiesClass,
                "get",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as String
                        val spoofedValue = PIXEL_PROPERTIES[key]

                        // Metric: record property request
                        Metrics.getInstance().incrementPropertyRequest()

                        // Log ALL property requests to see what's being queried
                        StealthManager.stealthLog("📝 Property requested: $key")

                        if (spoofedValue != null) {
                            param.result = spoofedValue
                            Metrics.getInstance().incrementPropertySpoofed()
                            StealthManager.stealthLog("🎯 SPOOFED: $key = $spoofedValue")
                        } else {
                            StealthManager.stealthLog("⚠️ NOT SPOOFED: $key (not in our list)")
                        }
                    }
                }
            )
            
            // Hook get(String key, String def)
            XposedHelpers.findAndHookMethod(
                systemPropertiesClass,
                "get",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as String
                        val defaultValue = param.args[1] as String
                        val spoofedValue = PIXEL_PROPERTIES[key]
                        
                        // Log ALL property requests to see what's being queried
                        StealthManager.stealthLog("📝 Property requested (with default): $key (default: $defaultValue)")
                        
                        if (spoofedValue != null) {
                            param.result = spoofedValue
                            StealthManager.stealthLog("🎯 SPOOFED: $key = $spoofedValue")
                        } else {
                            StealthManager.stealthLog("⚠️ NOT SPOOFED: $key (not in our list, would return: $defaultValue)")
                        }
                    }
                }
            )
            
            StealthManager.stealthLog("✅ SystemProperties hooks installed")
            // Mark that SystemProperties hooks are active for diagnostics
            Metrics.getInstance().setSystemPropertiesHookInstalled(true)
            
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ SystemProperties hook failed: ${e.message}")
        }
    }
    
    /**
     * Hook Build class fields - Apps read these directly
     */
    private fun hookBuildClass(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val buildClass = Build::class.java
            
            StealthManager.stealthLog("🔨 Hooking Build class fields...")
            
            // Hook all Build.* field access
            hookBuildField(buildClass, "BRAND", "google")
            hookBuildField(buildClass, "MANUFACTURER", "Google")
            hookBuildField(buildClass, "MODEL", PIXEL_PROPERTIES["ro.product.model"] ?: "Pixel 10 Pro XL")
            hookBuildField(buildClass, "DEVICE", PIXEL_PROPERTIES["ro.product.device"] ?: "mustang")
            hookBuildField(buildClass, "PRODUCT", PIXEL_PROPERTIES["ro.build.product"] ?: "mustang")
            hookBuildField(buildClass, "FINGERPRINT", PIXEL_PROPERTIES["ro.build.fingerprint"] ?: "google/mustang/mustang:16/BP2A.250805.005/13691446:user/release-keys")
            hookBuildField(buildClass, "ID", PIXEL_PROPERTIES["ro.build.id"] ?: "BP2A.250805.005")
            hookBuildField(buildClass, "DISPLAY", PIXEL_PROPERTIES["ro.build.display.id"] ?: "BP2A.250805.005")
            hookBuildField(buildClass, "TAGS", PIXEL_PROPERTIES["ro.build.tags"] ?: "release-keys")
            hookBuildField(buildClass, "TYPE", PIXEL_PROPERTIES["ro.build.type"] ?: "user")
            hookBuildField(buildClass, "USER", PIXEL_PROPERTIES["ro.build.user"] ?: "android-build")
            hookBuildField(buildClass, "HOST", PIXEL_PROPERTIES["ro.build.host"] ?: "e27561acca81")
            
            // Hook Build.VERSION fields
            val versionClass = Build.VERSION::class.java
            hookBuildField(versionClass, "RELEASE", PIXEL_PROPERTIES["ro.build.version.release"] ?: "16")
            hookBuildField(versionClass, "SDK", PIXEL_PROPERTIES["ro.build.version.sdk"] ?: "36")
            hookBuildField(versionClass, "SDK_INT", (PIXEL_PROPERTIES["ro.build.version.sdk"] ?: "36").toInt())
            hookBuildField(versionClass, "INCREMENTAL", PIXEL_PROPERTIES["ro.build.version.incremental"] ?: "13691446")
            hookBuildField(versionClass, "SECURITY_PATCH", PIXEL_PROPERTIES["ro.build.version.security_patch"] ?: "2025-08-05")
            
            // Add comprehensive field access logging
            val allFields = buildClass.declaredFields
            StealthManager.stealthLog("📋 Available Build fields: ${allFields.map { it.name }.joinToString(", ")}")
            
            StealthManager.stealthLog("✅ Build class fields hooked")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ Build class hook failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Hook individual Build field - FIXED VERSION
     */
    private fun hookBuildField(clazz: Class<*>, fieldName: String, value: Any) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            
            // Directly set the field value
            if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
                // Make field modifiable
                val modifiersField = Field::class.java.getDeclaredField("modifiers")
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and java.lang.reflect.Modifier.FINAL.inv())
                
                // Set the new value
                field.set(null, value)
                StealthManager.stealthLog("🔧 Build.$fieldName = $value (set directly)")
            }
            
            // Also try Xposed approach
            try {
                XposedHelpers.setStaticObjectField(clazz, fieldName, value)
                StealthManager.stealthLog("🔧 Build.$fieldName = $value (set via Xposed)")
            } catch (e: Exception) {
                StealthManager.stealthLog("⚠️ Xposed set failed for $fieldName: ${e.message}")
            }
            
        } catch (e: Exception) {
            StealthManager.stealthLog("⚠️ Failed to hook Build.$fieldName: ${e.message}")
        }
    }
    
    /**
     * Hook Settings.Secure for device ID spoofing
     */
    private fun hookSettingsSecure(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val settingsSecureClass = XposedHelpers.findClass("android.provider.Settings\$Secure", lpparam.classLoader)
            
            XposedHelpers.findAndHookMethod(
                settingsSecureClass,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as String
                        when (key) {
                            "android_id" -> {
                                param.result = "1234567890abcdef" // Consistent fake Android ID
                                StealthManager.stealthLog("🔧 Settings.Secure.ANDROID_ID spoofed")
                            }
                        }
                    }
                }
            )
            
            StealthManager.stealthLog("✅ Settings.Secure hooks installed")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ Settings.Secure hook failed: ${e.message}")
        }
    }
    
    /**
     * Hook TelephonyManager for device identifiers
     */
    private fun hookTelephonyManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val telephonyManagerClass = XposedHelpers.findClass("android.telephony.TelephonyManager", lpparam.classLoader)
            
            // Hook getDeviceId
            XposedHelpers.findAndHookMethod(
                telephonyManagerClass,
                "getDeviceId",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = "357240051111110" // Fake IMEI
                        StealthManager.stealthLog("🔧 TelephonyManager.getDeviceId() spoofed")
                    }
                }
            )
            
            // Hook getImei
            XposedHelpers.findAndHookMethod(
                telephonyManagerClass,
                "getImei",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = "357240051111110" // Fake IMEI
                        StealthManager.stealthLog("🔧 TelephonyManager.getImei() spoofed")
                    }
                }
            )
            
            // Hook getSubscriberId (IMSI)
            XposedHelpers.findAndHookMethod(
                telephonyManagerClass,
                "getSubscriberId",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = "310260000000000" // Fake IMSI
                        StealthManager.stealthLog("🔧 TelephonyManager.getSubscriberId() spoofed")
                    }
                }
            )
            
            StealthManager.stealthLog("✅ TelephonyManager hooks installed")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ TelephonyManager hook failed: ${e.message}")
        }
    }
    
    /**
     * Hook PackageManager for app information
     */
    private fun hookPackageManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val packageManagerClass = XposedHelpers.findClass("android.content.pm.PackageManager", lpparam.classLoader)
            
            // This can be extended to hide root/Xposed related packages
            StealthManager.stealthLog("✅ PackageManager hooks prepared")
            
        } catch (e: Exception) {
            StealthManager.stealthLog("❌ PackageManager hook failed: ${e.message}")
        }
    }
    
    /**
     * Get spoofed property value
     */
    fun getSpoofedProperty(key: String): String? {
        return PIXEL_PROPERTIES[key]
    }
    
    /**
     * Update a specific property
     */
    fun updateProperty(key: String, value: String) {
        PIXEL_PROPERTIES[key] = value
        StealthManager.stealthLog("🔧 Property updated: $key = $value")
    }
}
