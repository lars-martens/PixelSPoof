package com.kashi.caimanspoof.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kashi.caimanspoof.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class for system information
 */
data class SystemInfo(
    val versionName: String,
    val versionCode: String
)

/**
 * Get system information safely
 */
private fun getSystemInfo(context: android.content.Context): SystemInfo? {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        SystemInfo(
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = packageInfo.versionCode.toString()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Diagnostics dashboard screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val perAppManager = remember { PerAppConfigManager.getInstance(context) }
    val configManager = remember { ConfigManager.getInstance(context) }

    // Live metrics state
    var metrics by remember { mutableStateOf(Metrics.getInstance()) }
    var isLSPosedEnabled by remember { mutableStateOf(false) }
    var spoofingStatus by remember { mutableStateOf("Checking...") }

    // Update metrics periodically
    LaunchedEffect(Unit) {
        while (true) {
            metrics = Metrics.getInstance()
            isLSPosedEnabled = LSPosedDiagnostic.isModuleEnabledForPackage(context.packageName)
            spoofingStatus = getSpoofingStatus(metrics, isLSPosedEnabled)
            delay(2000) // Update every 2 seconds
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Back arrow
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Overview
            item {
                StatusOverviewCard(
                    isLSPosedEnabled = isLSPosedEnabled,
                    spoofingStatus = spoofingStatus,
                    metrics = metrics
                )
            }

            // Runtime Metrics
            item {
                RuntimeMetricsCard(metrics = metrics)
            }

            // Per-App Statistics
            item {
                PerAppStatsCard(perAppManager = perAppManager)
            }

            // System Information
            item {
                SystemInfoCard()
            }

            // Troubleshooting
            item {
                TroubleshootingCard()
            }
        }
    }
}

/**
 * Status overview card
 */
@Composable
fun StatusOverviewCard(
    isLSPosedEnabled: Boolean,
    spoofingStatus: String,
    metrics: Metrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLSPosedEnabled && metrics.systemPropertiesHookInstalled)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "System Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusItem(
                label = "LSPosed Module",
                value = if (isLSPosedEnabled) "Enabled" else "Disabled",
                isGood = isLSPosedEnabled
            )

            StatusItem(
                label = "System Hooks",
                value = if (metrics.systemPropertiesHookInstalled) "Installed" else "Not Installed",
                isGood = metrics.systemPropertiesHookInstalled
            )

            StatusItem(
                label = "Spoofing Status",
                value = spoofingStatus,
                isGood = spoofingStatus.contains("Active")
            )

            StatusItem(
                label = "Network Success Rate",
                value = "${metrics.calculateNetworkSuccessRate()}%",
                isGood = metrics.calculateNetworkSuccessRate() > 80
            )
        }
    }
}

/**
 * Runtime metrics card
 */
@Composable
fun RuntimeMetricsCard(metrics: Metrics) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Runtime Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricItem(
                label = "Property Requests",
                value = metrics.propertyRequests.toString()
            )

            MetricItem(
                label = "Properties Spoofed",
                value = metrics.propertiesSpoofed.toString()
            )

            MetricItem(
                label = "Network Requests Intercepted",
                value = metrics.requestsIntercepted.toString()
            )

            MetricItem(
                label = "Headers Spoofed",
                value = metrics.headersSpoofed.toString()
            )

            MetricItem(
                label = "SSL Bypass Available",
                value = if (metrics.sslBypassAvailable) "Yes" else "No"
            )
        }
    }
}

/**
 * Per-app statistics card
 */
@Composable
fun PerAppStatsCard(perAppManager: PerAppConfigManager) {
    val appConfigs by perAppManager.appConfigs.collectAsState()
    val installedApps by perAppManager.installedApps.collectAsState()

    val enabledApps = appConfigs.count { it.value.enabled }
    val totalApps = installedApps.size
    val customProfiles = appConfigs.count { it.value.profileName != null }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Per-App Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricItem(
                label = "Total Apps",
                value = totalApps.toString()
            )

            MetricItem(
                label = "Apps with Spoofing Enabled",
                value = "$enabledApps / $totalApps"
            )

            MetricItem(
                label = "Custom Profiles",
                value = customProfiles.toString()
            )

            if (enabledApps > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enabled Apps:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                appConfigs.filter { it.value.enabled }.forEach { (packageName, config) ->
                    val appName = installedApps.find { it.packageName == packageName }?.appName ?: packageName
                    Text(
                        text = "• $appName${config.profileName?.let { " (${it})" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * System information card
 */
@Composable
fun SystemInfoCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val systemInfo = remember { getSystemInfo(context) }

            if (systemInfo != null) {
                MetricItem(
                    label = "PixelSpoof Version",
                    value = systemInfo.versionName
                )

                MetricItem(
                    label = "Build Number",
                    value = systemInfo.versionCode
                )

                MetricItem(
                    label = "Android API Level",
                    value = android.os.Build.VERSION.SDK_INT.toString()
                )

                MetricItem(
                    label = "Android Version",
                    value = android.os.Build.VERSION.RELEASE
                )

                MetricItem(
                    label = "Device Model",
                    value = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                )
            } else {
                Text(
                    text = "Unable to retrieve system information",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Troubleshooting card
 */
@Composable
fun TroubleshootingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Troubleshooting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Common Issues:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TroubleshootingItem(
                issue = "LSPosed module not enabled",
                solution = "Enable PixelSpoof in LSPosed Manager and reboot"
            )

            TroubleshootingItem(
                issue = "No property requests detected",
                solution = "Check if target apps are running and accessing device properties"
            )

            TroubleshootingItem(
                issue = "Low network success rate",
                solution = "Verify network permissions and SSL certificate handling"
            )

            TroubleshootingItem(
                issue = "Per-app config not working",
                solution = "Ensure app is restarted after changing configuration"
            )
        }
    }
}

/**
 * Status item composable
 */
@Composable
fun StatusItem(
    label: String,
    value: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Status indicator
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(8.dp)
            ) {}
        }
    }
}

/**
 * Metric item composable
 */
@Composable
fun MetricItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Troubleshooting item composable
 */
@Composable
fun TroubleshootingItem(
    issue: String,
    solution: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "• $issue",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = solution,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

/**
 * Get spoofing status description
 */
private fun getSpoofingStatus(metrics: Metrics, isLSPosedEnabled: Boolean): String {
    return when {
        !isLSPosedEnabled -> "LSPosed Module Disabled"
        !metrics.systemPropertiesHookInstalled -> "Hooks Not Installed"
        metrics.propertyRequests == 0 -> "Waiting for Requests"
        metrics.propertiesSpoofed > 0 -> "Active - Spoofing ${metrics.propertiesSpoofed} properties"
        else -> "Monitoring - No spoofing detected yet"
    }
}
