/*
 * PixelSpoof
 * Copyright (C) 2024 kashi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.kashi.caimanspoof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the configuration screen using MVVM architecture
 */
class ConfigViewModel(application: Application) : AndroidViewModel(application) {
    
    private val configManager = ConfigManager.getInstance(application.applicationContext)
    
    // Expose configuration state as StateFlow
    val availableProfiles: StateFlow<List<DeviceProfile>> = configManager.availableProfiles
    val selectedProfile: StateFlow<DeviceProfile?> = configManager.selectedProfile
    val isLoading: StateFlow<Boolean> = configManager.isLoading
    val lastError: StateFlow<String?> = configManager.lastError
    
    /**
     * Select a new device profile
     */
    fun selectProfile(profileName: String) {
        viewModelScope.launch {
            try {
                // Update ConfigManager synchronously
                configManager.setSelectedProfileSync(profileName)
                
                // Get the updated profile
                val profile = configManager.getCurrentProfileSync()
                
                // Log the profile switch attempt
                android.util.Log.d("PixelSpoof", "Profile switch requested: $profileName -> ${profile.displayName}")
                
                // Update DeviceProfileManager and PropertySpoofer
                if (profile.displayName == profileName) {
                    val success = DeviceProfileManager.getInstance().switchProfile(profile.device, null)
                    if (success) {
                        // Force refresh PropertySpoofer with new profile
                        PropertySpoofer.getInstance().refreshPropertiesForProfile(profile)
                        android.util.Log.d("PixelSpoof", "Profile switched successfully to: ${profile.displayName}")
                    } else {
                        android.util.Log.e("PixelSpoof", "DeviceProfileManager.switchProfile failed")
                    }
                } else {
                    android.util.Log.e("PixelSpoof", "Profile name mismatch: requested $profileName, got ${profile.displayName}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PixelSpoof", "Profile switch error: ${e.message}")
                // Error handling is managed by ConfigManager
            }
        }
    }
    
    /**
     * Refresh profiles from server
     */
    fun refreshProfiles() {
        viewModelScope.launch {
            try {
                configManager.forceRefresh()
            } catch (e: Exception) {
                // Error handling is managed by ConfigManager
            }
        }
    }
    
    /**
     * Set stealth mode
     */
    fun setStealthMode(enabled: Boolean) {
        configManager.setStealthMode(enabled)
    }
    
    /**
     * Check if stealth mode is enabled
     */
    fun isStealthModeEnabled(): Boolean {
        return configManager.isStealthModeEnabled()
    }
    
    /**
     * Set auto update
     */
    fun setAutoUpdate(enabled: Boolean) {
        configManager.setAutoUpdate(enabled)
    }
    
    /**
     * Check if auto update is enabled
     */
    fun isAutoUpdateEnabled(): Boolean {
        return configManager.isAutoUpdateEnabled()
    }
    
    override fun onCleared() {
        super.onCleared()
        configManager.cleanup()
    }
}
