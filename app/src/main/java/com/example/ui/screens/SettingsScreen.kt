package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.ui.components.KeepsakeTopBar
import com.example.ui.components.RomanticDatePickerDialog
import com.example.ui.components.SunsetBackdrop
import com.example.util.DateHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    prefs: AppPreferences,
    statusMessage: String?,
    onClearStatusMessage: () -> Unit,
    onBack: () -> Unit,
    onSetDarkMode: (Boolean) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetMusicEnabled: (Boolean) -> Unit,
    onSetInactivityTimeout: (Int) -> Unit,
    onSetStartDate: (String) -> Unit,
    onSetAnniversaryDate: (String) -> Unit,
    onUpdateHeroPhoto: (Uri) -> Unit,
    onResetHeroPhoto: () -> Unit,
    onChangePassword: (current: String, next: String, confirm: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onExportBackup: (Uri) -> Unit,
    onImportBackup: (Uri) -> Unit,
    onLockNow: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(false) }
    var pickingAnniversaryDate by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        if (!statusMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(statusMessage)
            onClearStatusMessage()
        }
    }

    val heroPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUpdateHeroPhoto(uri)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri)
    }

    SunsetBackdrop(darkMode = prefs.darkMode) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { KeepsakeTopBar(title = "Settings & Privacy", onBack = onBack) },
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Appearance & Reading
                SettingsSectionCard(title = "Appearance & Reading") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Candlelight Dark Mode", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Switch between warm sunset cream and deep velvet dark theme",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.darkMode,
                            onCheckedChange = onSetDarkMode,
                            modifier = Modifier.testTag("settings_dark_mode_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = "Reading Font Size (${(prefs.readingFontScale * 100).roundToInt()}%)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = prefs.readingFontScale,
                        onValueChange = onSetFontScale,
                        valueRange = 0.85f..1.45f,
                        steps = 5,
                        modifier = Modifier.testTag("font_scale_slider")
                    )
                    Text(
                        text = "Sample chapter & letter reading size.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (16 * prefs.readingFontScale).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Romantic Background Music", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Gentle offline harp melody (disabled by default)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = prefs.musicEnabled,
                            onCheckedChange = onSetMusicEnabled,
                            modifier = Modifier.testTag("settings_music_switch")
                        )
                    }
                }

                // Couple Dates & Cover Photo
                SettingsSectionCard(title = "Our Dates & Cover Portrait") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Relationship Starting Date", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (prefs.relationshipStartDateIso.isBlank()) {
                                    "Not set yet"
                                } else {
                                    DateHelper.formatDisplayDate(prefs.relationshipStartDateIso)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(onClick = { pickingStartDate = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Anniversary Date", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (prefs.anniversaryDateIso.isBlank()) {
                                    "Not set yet"
                                } else {
                                    DateHelper.formatDisplayDate(prefs.anniversaryDateIso)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(onClick = { pickingAnniversaryDate = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text("Homepage Couple Photograph", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                heroPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Photo")
                        }
                        if (prefs.heroPhotoPath.isNotBlank()) {
                            TextButton(onClick = onResetHeroPhoto) {
                                Text("Reset Default")
                            }
                        }
                    }
                }

                // Privacy & Password Security
                SettingsSectionCard(title = "Privacy & Password Protection") {
                    Text(
                        text = "Auto-Lock After Inactivity",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Immediate", 1 to "1 min", 5 to "5 min", 15 to "15 min").forEach { (min, label) ->
                            FilterChip(
                                selected = prefs.inactivityTimeoutMinutes == min,
                                onClick = { onSetInactivityTimeout(min) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_password_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Keepsake Password")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onLockNow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Lock Private Content Now")
                    }
                }

                // Offline Backup & Restore
                SettingsSectionCard(title = "Offline Backup & Restore") {
                    Text(
                        text = "Export or restore your private keepsake to a local file on your device. Backups are never uploaded or shared automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { exportLauncher.launch("journey_of_two_souls_backup.json") },
                            modifier = Modifier.weight(1f).testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore")
                        }
                    }
                }
            }
        }
    }

    if (pickingStartDate) {
        RomanticDatePickerDialog(
            onDateSelected = onSetStartDate,
            onDismiss = { pickingStartDate = false }
        )
    }
    if (pickingAnniversaryDate) {
        RomanticDatePickerDialog(
            onDateSelected = onSetAnniversaryDate,
            onDismiss = { pickingAnniversaryDate = false }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSubmit = { current, next, confirm, setDialogError ->
                onChangePassword(current, next, confirm) { success, message ->
                    if (success) {
                        showChangePasswordDialog = false
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    } else {
                        setDialogError(message)
                    }
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, (String) -> Unit) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val vis = if (showPasswords) VisualTransformation.None else PasswordVisualTransformation()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it; errorText = null },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = vis,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("current_password_input")
                )
                OutlinedTextField(
                    value = next,
                    onValueChange = { next = it; errorText = null },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = vis,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("new_password_input")
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; errorText = null },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = vis,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_password_input")
                )
                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(current, next, confirm) { msg -> errorText = msg }
                },
                modifier = Modifier.testTag("submit_change_password_button")
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
