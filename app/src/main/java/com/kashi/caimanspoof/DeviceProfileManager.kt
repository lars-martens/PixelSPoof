package com.kashi.caimanspoof

import android.content.Context
import kotlinx.coroutines.*
import java.io.File

/**
 * Device profile management system
 * Handles loading, caching, and switching between device profiles
 */
class DeviceProfileManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: DeviceProfileManager? = null

        fun getInstance(): DeviceProfileManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceProfileManager().also { INSTANCE = it }
            }
        }
    }

    private val profileCache = mutableMapOf<String, DeviceProfile>()
    private var currentProfile: DeviceProfile? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Initialize profile manager
     */
    fun initialize(context: Context?) {
        ErrorHandler.safeExecute("Profile manager initialization", "DeviceProfileManager") {
            loadDefaultProfiles()
            loadCurrentProfile(context)
            ErrorHandler.logSuccess("Profile manager initialized", "DeviceProfileManager")
        }
    }

    /**
     * Load default Pixel device profiles
     */
    private fun loadDefaultProfiles() {
        val defaultProfiles = listOf(
            DeviceProfile.getPixel10ProXL(),
            DeviceProfile.getPixel10Pro(),
            DeviceProfile.getPixel10(),
            DeviceProfile.getPixel9ProXL(),
            DeviceProfile.getPixel9Pro(),
            DeviceProfile.getPixel8Pro()
        )

        defaultProfiles.forEach { profile ->
            profileCache[profile.device] = profile
        }

        ErrorHandler.logSuccess("Default profiles loaded", "DeviceProfileManager", "${defaultProfiles.size} profiles")
    }

    /**
     * Load current profile from configuration
     */
    private fun loadCurrentProfile(context: Context?) {
        ErrorHandler.safeExecute("Current profile loading", "DeviceProfileManager") {
            val configManager = ConfigManager.getInstance(context)
            val savedProfile = configManager?.getCurrentProfileSync()

            currentProfile = savedProfile ?: DeviceProfile.getPixel10ProXL()

            ErrorHandler.logSuccess("Current profile loaded", "DeviceProfileManager", currentProfile?.displayName)
        }
    }

    /**
     * Get current active profile
     */
    fun getCurrentProfile(): DeviceProfile? {
        return currentProfile
    }

    /**
     * Get current profile synchronously (for Xposed hooks)
     */
    fun getCurrentProfileSync(): DeviceProfile {
        return currentProfile ?: DeviceProfile.getPixel10ProXL()
    }

    /**
     * Switch to different profile
     */
    fun switchProfile(deviceKey: String, context: Context?): Boolean {
        return ErrorHandler.safeExecuteWithResult(
            "Profile switch to $deviceKey",
            "DeviceProfileManager",
            false
        ) {
            val newProfile = profileCache[deviceKey]
            if (newProfile != null) {
                currentProfile = newProfile

                // Save to configuration using synchronous method
                ConfigManager.getInstance(context)?.setSelectedProfileSync(newProfile.displayName)

                // Refresh PropertySpoofer with new profile
                PropertySpoofer.getInstance().refreshPropertiesForProfile(newProfile)

                ErrorHandler.logSuccess("Profile switched", "DeviceProfileManager", newProfile.displayName)
                true
            } else {
                ErrorHandler.logError("Profile switch failed", "DeviceProfileManager", IllegalArgumentException("Profile not found: $deviceKey"))
                false
            }
        }
    }

    /**
     * Get all available profiles
     */
    fun getAvailableProfiles(): List<DeviceProfile> {
        return profileCache.values.toList()
    }

    /**
     * Get profile by device key
     */
    fun getProfile(deviceKey: String): DeviceProfile? {
        return profileCache[deviceKey]
    }

    /**
     * Add custom profile
     */
    fun addCustomProfile(profile: DeviceProfile) {
        ErrorHandler.safeExecute("Add custom profile", "DeviceProfileManager") {
            profileCache[profile.device] = profile
            ErrorHandler.logSuccess("Custom profile added", "DeviceProfileManager", profile.displayName)
        }
    }

    /**
     * Remove custom profile
     */
    fun removeCustomProfile(deviceKey: String) {
        ErrorHandler.safeExecute("Remove custom profile", "DeviceProfileManager") {
            if (profileCache.remove(deviceKey) != null) {
                ErrorHandler.logSuccess("Custom profile removed", "DeviceProfileManager", deviceKey)
            }
        }
    }

    /**
     * Validate profile integrity
     */
    fun validateProfile(profile: DeviceProfile): Boolean {
        return ErrorHandler.safeExecuteWithResult(
            "Profile validation",
            "DeviceProfileManager",
            false
        ) {
            val requiredFields = listOf(
                profile.manufacturer,
                profile.brand,
                profile.model,
                profile.device,
                profile.fingerprint,
                profile.buildId
            )

            val isValid = requiredFields.all { it.isNotBlank() }

            if (isValid) {
                ErrorHandler.logSuccess("Profile validated", "DeviceProfileManager", profile.displayName)
            } else {
                ErrorHandler.logError("Profile validation failed", "DeviceProfileManager", IllegalArgumentException("Missing required fields"))
            }

            isValid
        }
    }

    /**
     * Get profile statistics
     */
    fun getProfileStats(): ProfileStats {
        val totalProfiles = profileCache.size
        val customProfiles = profileCache.values.count { it.isCustom }
        val pixelProfiles = profileCache.values.count { it.brand == "google" }

        return ProfileStats(
            totalProfiles = totalProfiles,
            customProfiles = customProfiles,
            pixelProfiles = pixelProfiles,
            currentProfile = currentProfile?.displayName ?: "None"
        )
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        coroutineScope.cancel()
        profileCache.clear()
        currentProfile = null
    }

    /**
     * Profile statistics
     */
    data class ProfileStats(
        val totalProfiles: Int,
        val customProfiles: Int,
        val pixelProfiles: Int,
        val currentProfile: String
    )
}
