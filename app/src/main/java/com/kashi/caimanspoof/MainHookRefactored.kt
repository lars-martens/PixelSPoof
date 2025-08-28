package com.kashi.caimanspoof

import android.content.Context
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Refactored Main Hook - Focused on Effective Spoofing Methods
 * Uses modern architecture with proper error handling and safety checks
 */
class MainHookRefactored : IXposedHookLoadPackage {

    companion object {
        private var isInitialized = false
        private val criticalApps = setOf(
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.android.vending"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val startTime = System.currentTimeMillis()
        ErrorHandler.logStart("Package hooking", "MainHookRefactored")

        try {
            // Safety check first
            val safetyResult = SafetyValidator.getInstance().validateEnvironment(lpparam)
            if (!safetyResult.allPassed) {
                ErrorHandler.logError("Safety check failed", "MainHookRefactored",
                    Exception("Safety validation failed: ${safetyResult.failures.joinToString { it.name }}"))
                return
            }

            // Initialize systems once
            if (!isInitialized) {
                initializeSystems(lpparam)
            }

            val packageName = lpparam.packageName
            val isCriticalApp = packageName in criticalApps

            // Record package hook attempt
            Metrics.getInstance().recordPackageHook(packageName)

            StealthManager.stealthLog("🎯 PIXELSPOOF ACTIVE - Hooking: $packageName")

            // LSPosed/Xposed enablement diagnostic
            val lsposedOk = LSPosedDiagnostic.isModuleEnabledForPackage(packageName)
            if (!lsposedOk) {
                StealthManager.stealthLog("⚠️ Module may NOT be enabled for $packageName (check LSPosed settings)")
            }

            // Apply effective spoofing methods
            applyEffectiveSpoofing(lpparam, isCriticalApp)

            // Runtime validation
            validateSpoofingEffectiveness(lpparam)

            ErrorHandler.logCompletion("Package hooking", "MainHookRefactored", startTime)

        } catch (e: Exception) {
            ErrorHandler.logError("Package hooking failed", "MainHookRefactored", e)
        }
    }

    /**
     * Initialize all systems with proper error handling
     */
    private fun initializeSystems(lpparam: XC_LoadPackage.LoadPackageParam) {
        ErrorHandler.safeExecute("System initialization", "MainHookRefactored") {
            // Initialize stealth manager
            StealthManager.getInstance()

            // Get context safely
            val context = getApplicationContext(lpparam)

            // Initialize profile manager
            DeviceProfileManager.getInstance().initialize(context)

            // Initialize context acquisition (this actually works!)
            ContextAcquisitionBypass.getInstance().initializeContextBypass(lpparam)

            isInitialized = true
            ErrorHandler.logSuccess("Systems initialized", "MainHookRefactored")
        }
    }

    /**
     * Apply only the effective spoofing methods
     */
    private fun applyEffectiveSpoofing(lpparam: XC_LoadPackage.LoadPackageParam, isCriticalApp: Boolean) {
        val profile = DeviceProfileManager.getInstance().getCurrentProfileSync()

        // 1. Property Spoofing - This actually works!
        ErrorHandler.safeExecute("Property spoofing", "MainHookRefactored") {
            PropertySpoofer.getInstance().initializePropertySpoofing(lpparam, profile)
        }

        // 2. Network Interception - Useful for many apps
        if (isCriticalApp) {
            ErrorHandler.safeExecute("Network interception", "MainHookRefactored") {
                NetworkInterceptor.getInstance().initializeNetworkInterception(lpparam, profile)
            }
        }

        // 3. System Integrity Spoofing - Some value for basic checks
        ErrorHandler.safeExecute("System integrity spoofing", "MainHookRefactored") {
            SystemIntegritySpoofer.getInstance().initializeIntegritySpoof(lpparam, profile)
        }

        // NOTE: AttestationBypass and KernelLevelBypass are DISABLED
        // They target hardware-backed systems that cannot be bypassed from userspace
        logDisabledMethods()
    }

    /**
     * Validate that spoofing is working
     */
    private fun validateSpoofingEffectiveness(lpparam: XC_LoadPackage.LoadPackageParam) {
        ErrorHandler.safeExecute("Spoofing validation", "MainHookRefactored") {
            val profile = DeviceProfileManager.getInstance().getCurrentProfileSync()
            val validationResult = SpoofingValidator.getInstance().validateSpoofing(lpparam, profile)

            if (validationResult.allPassed) {
                StealthManager.stealthLog("✅ Spoofing validation passed")
            } else {
                StealthManager.stealthLog("⚠️ Some validations failed: ${validationResult.summary}")
            }
        }
    }

    /**
     * Get application context safely
     */
    private fun getApplicationContext(lpparam: XC_LoadPackage.LoadPackageParam): Context? {
        return ErrorHandler.safeExecuteWithResult(
            "Context acquisition",
            "MainHookRefactored",
            null
        ) {
            try {
                val activityThread = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader),
                    "currentApplication"
                )
                XposedHelpers.callMethod(activityThread, "getApplicationContext") as? Context
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Log information about disabled ineffective methods
     */
    private fun logDisabledMethods() {
        StealthManager.stealthLog("ℹ️ FOCUSING ON EFFECTIVE METHODS:")
        StealthManager.stealthLog("✅ Property Spoofing - Actually intercepts system calls")
        StealthManager.stealthLog("✅ Context Acquisition - Solves context issues")
        StealthManager.stealthLog("✅ Network Interception - Spoofs HTTP headers")
        StealthManager.stealthLog("🚫 Hardware Attestation - Cannot be bypassed from userspace")
        StealthManager.stealthLog("🚫 Kernel Modifications - Too risky and often ineffective")
    }
}
