package com.kashi.caimanspoof.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kashi.caimanspoof.*
import kotlinx.coroutines.launch

/**
 * Per-app configuration screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerAppConfigScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val perAppManager = remember { PerAppConfigManager.getInstance(context) }
    val configManager = remember { ConfigManager.getInstance(context) }

    val installedApps by perAppManager.installedApps.collectAsState()
    val availableProfiles by configManager.availableProfiles.collectAsState()
    val appConfigs by perAppManager.appConfigs.collectAsState()

    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Per-App Configuration") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Back arrow would go here
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Per-App Spoofing",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure different device profiles for specific apps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Apps list
            items(installedApps) { app ->
                AppConfigCard(
                    app = app,
                    config = appConfigs[app.packageName] ?: AppSpoofConfig(
                        packageName = app.packageName,
                        enabled = false,
                        profileName = null,
                        customProperties = emptyMap()
                    ),
                    onToggleSpoof = { enabled ->
                        perAppManager.setAppSpoofEnabled(app.packageName, enabled)
                    },
                    onSelectProfile = {
                        selectedApp = app
                        showProfileDialog = true
                    },
                    onViewDetails = {
                        selectedApp = app
                        // Could show detailed config dialog
                    }
                )
            }
        }
    }

    // Profile selection dialog
    if (showProfileDialog && selectedApp != null) {
        ProfileSelectionDialog(
            app = selectedApp!!,
            availableProfiles = availableProfiles,
            currentConfig = appConfigs[selectedApp!!.packageName],
            onProfileSelected = { profileName ->
                perAppManager.setAppProfile(selectedApp!!.packageName, profileName)
                showProfileDialog = false
                selectedApp = null
            },
            onDismiss = {
                showProfileDialog = false
                selectedApp = null
            }
        )
    }
}

/**
 * App configuration card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigCard(
    app: AppInfo,
    config: AppSpoofConfig,
    onToggleSpoof: (Boolean) -> Unit,
    onSelectProfile: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (app.isSystemApp) {
                        Text(
                            text = "System App",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Switch(
                    checked = config.enabled,
                    onCheckedChange = onToggleSpoof
                )
            }

            if (config.enabled) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = config.profileName ?: "Use Global",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    TextButton(
                        onClick = onViewDetails
                    ) {
                        Text("Details")
                    }
                }
            }
        }
    }
}

/**
 * Profile selection dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSelectionDialog(
    app: AppInfo,
    availableProfiles: List<DeviceProfile>,
    currentConfig: AppSpoofConfig?,
    onProfileSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Profile for ${app.appName}")
        },
        text = {
            LazyColumn {
                // Global option
                item {
                    ProfileOptionItem(
                        profileName = "Use Global Profile",
                        isSelected = currentConfig?.profileName == null,
                        onClick = { onProfileSelected(null) }
                    )
                }

                // Available profiles
                items(availableProfiles) { profile ->
                    ProfileOptionItem(
                        profileName = profile.displayName,
                        isSelected = currentConfig?.profileName == profile.displayName,
                        onClick = { onProfileSelected(profile.displayName) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Profile option item
 */
@Composable
fun ProfileOptionItem(
    profileName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = profileName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
