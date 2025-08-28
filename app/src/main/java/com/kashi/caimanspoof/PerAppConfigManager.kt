package com.kashi.caimanspoof

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Per-app configuration manager for fine-grained spoofing control
 */
class PerAppConfigManager private constructor(private val context: Context?) {

    companion object {
        private const val PREF_NAME = "pixelspoof_per_app_config"
        private const val KEY_APP_CONFIGS = "app_configs"

        @Volatile
        private var INSTANCE: PerAppConfigManager? = null

        fun getInstance(context: Context? = null): PerAppConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerAppConfigManager(context).also { INSTANCE = it }
            }
        }
    }

    private val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val packageManager = context?.packageManager

    // Per-app configurations
    private val _appConfigs = MutableStateFlow<Map<String, AppSpoofConfig>>(emptyMap())
    val appConfigs: StateFlow<Map<String, AppSpoofConfig>> = _appConfigs.asStateFlow()

    // Installed apps list
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    init {
        loadConfigurations()
        loadInstalledApps()
    }

    /**
     * Load saved per-app configurations
     */
    private fun loadConfigurations() {
        try {
            val configsJson = prefs?.getString(KEY_APP_CONFIGS, "{}") ?: "{}"
            val configs = mutableMapOf<String, AppSpoofConfig>()

            val jsonObject = org.json.JSONObject(configsJson)
            jsonObject.keys().forEach { packageName ->
                val configJson = jsonObject.getJSONObject(packageName)
                configs[packageName] = AppSpoofConfig.fromJson(configJson)
            }

            _appConfigs.value = configs
        } catch (e: Exception) {
            // Fallback to empty configs
            _appConfigs.value = emptyMap()
        }
    }

    /**
     * Load installed apps
     */
    private fun loadInstalledApps() {
        try {
            val apps = mutableListOf<AppInfo>()
            val packages = packageManager?.getInstalledPackages(PackageManager.GET_META_DATA) ?: emptyList()

            for (pkg in packages) {
                if (pkg.packageName.startsWith("com.android.") ||
                    pkg.packageName.startsWith("android.") ||
                    pkg.packageName == context?.packageName) {
                    continue // Skip system apps and self
                }

                apps.add(AppInfo(
                    packageName = pkg.packageName,
                    appName = pkg.applicationInfo?.loadLabel(packageManager!!).toString(),
                    isSystemApp = (pkg.applicationInfo?.flags ?: 0) and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0,
                    spoofEnabled = _appConfigs.value[pkg.packageName]?.enabled ?: false
                ))
            }

            _installedApps.value = apps.sortedBy { it.appName }
        } catch (e: Exception) {
            _installedApps.value = emptyList()
        }
    }

    /**
     * Get configuration for specific app
     */
    fun getAppConfig(packageName: String): AppSpoofConfig {
        return _appConfigs.value[packageName] ?: AppSpoofConfig(
            packageName = packageName,
            enabled = false,
            profileName = null,
            customProperties = emptyMap()
        )
    }

    /**
     * Set configuration for specific app
     */
    fun setAppConfig(packageName: String, config: AppSpoofConfig) {
        val newConfigs = _appConfigs.value.toMutableMap()
        newConfigs[packageName] = config
        _appConfigs.value = newConfigs

        // Save to preferences
        saveConfigurations()

        // Update installed apps list
        updateAppSpoofStatus(packageName, config.enabled)
    }

    /**
     * Enable/disable spoofing for app
     */
    fun setAppSpoofEnabled(packageName: String, enabled: Boolean) {
        val currentConfig = getAppConfig(packageName)
        val newConfig = currentConfig.copy(enabled = enabled)
        setAppConfig(packageName, newConfig)
    }

    /**
     * Set custom profile for app
     */
    fun setAppProfile(packageName: String, profileName: String?) {
        val currentConfig = getAppConfig(packageName)
        val newConfig = currentConfig.copy(profileName = profileName)
        setAppConfig(packageName, newConfig)
    }

    /**
     * Add custom property for app
     */
    fun addCustomProperty(packageName: String, key: String, value: String) {
        val currentConfig = getAppConfig(packageName)
        val newProperties = currentConfig.customProperties.toMutableMap()
        newProperties[key] = value

        val newConfig = currentConfig.copy(customProperties = newProperties)
        setAppConfig(packageName, newConfig)
    }

    /**
     * Remove custom property for app
     */
    fun removeCustomProperty(packageName: String, key: String) {
        val currentConfig = getAppConfig(packageName)
        val newProperties = currentConfig.customProperties.toMutableMap()
        newProperties.remove(key)

        val newConfig = currentConfig.copy(customProperties = newProperties)
        setAppConfig(packageName, newConfig)
    }

    /**
     * Get effective profile for app (custom or global default)
     */
    fun getEffectiveProfile(packageName: String): DeviceProfile {
        val appConfig = getAppConfig(packageName)

        return if (appConfig.enabled && appConfig.profileName != null) {
            // Find custom profile for this app
            DeviceProfile.getAllProfiles().find { it.displayName == appConfig.profileName }
                ?: ConfigManager.getInstance(context).getCurrentProfileSync()
        } else {
            // Use global default
            ConfigManager.getInstance(context).getCurrentProfileSync()
        }
    }

    /**
     * Save configurations to preferences
     */
    private fun saveConfigurations() {
        try {
            val jsonObject = org.json.JSONObject()
            _appConfigs.value.forEach { (packageName, config) ->
                jsonObject.put(packageName, config.toJson())
            }

            prefs?.edit()?.putString(KEY_APP_CONFIGS, jsonObject.toString())?.apply()
        } catch (e: Exception) {
            // Handle save error
        }
    }

    /**
     * Update app spoof status in installed apps list
     */
    private fun updateAppSpoofStatus(packageName: String, enabled: Boolean) {
        val currentApps = _installedApps.value.toMutableList()
        val appIndex = currentApps.indexOfFirst { it.packageName == packageName }

        if (appIndex >= 0) {
            currentApps[appIndex] = currentApps[appIndex].copy(spoofEnabled = enabled)
            _installedApps.value = currentApps
        }
    }

    /**
     * Refresh installed apps list
     */
    fun refreshInstalledApps() {
        loadInstalledApps()
    }
}

/**
 * Configuration for per-app spoofing
 */
data class AppSpoofConfig(
    val packageName: String,
    val enabled: Boolean,
    val profileName: String?,
    val customProperties: Map<String, String>
) {
    fun toJson(): org.json.JSONObject {
        return org.json.JSONObject().apply {
            put("packageName", packageName)
            put("enabled", enabled)
            put("profileName", profileName)
            put("customProperties", org.json.JSONObject(customProperties))
        }
    }

    companion object {
        fun fromJson(json: org.json.JSONObject): AppSpoofConfig {
            val customProps = mutableMapOf<String, String>()
            val propsJson = json.optJSONObject("customProperties")
            propsJson?.keys()?.forEach { key ->
                customProps[key] = propsJson.getString(key)
            }

            return AppSpoofConfig(
                packageName = json.getString("packageName"),
                enabled = json.getBoolean("enabled"),
                profileName = json.optString("profileName").takeIf { it.isNotEmpty() },
                customProperties = customProps
            )
        }
    }
}

/**
 * Information about installed app
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val spoofEnabled: Boolean
)
