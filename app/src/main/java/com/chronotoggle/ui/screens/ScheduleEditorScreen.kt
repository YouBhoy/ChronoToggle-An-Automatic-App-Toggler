package com.chronotoggle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chronotoggle.data.model.SettingType
import com.chronotoggle.viewmodel.ScheduleEditorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    state: ScheduleEditorState,
    isEditing: Boolean,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    onSettingTypeChanged: (SettingType) -> Unit,
    onTargetValueChanged: (String) -> Unit,
    onLabelChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = false
    )

    // Sync time picker state changes back to ViewModel
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onHourChanged(timePickerState.hour)
        onMinuteChanged(timePickerState.minute)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Schedule" else "New Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Label ---
            OutlinedTextField(
                value = state.label,
                onValueChange = onLabelChanged,
                label = { Text("Label (optional)") },
                placeholder = { Text("e.g., Night mode") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // --- Time Picker ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Schedule Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                }
            }

            // --- Setting Type Selector ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Setting to Change",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingType.entries.forEach { type ->
                        SettingTypeOption(
                            type = type,
                            isSelected = state.settingType == type,
                            onClick = { onSettingTypeChanged(type) }
                        )
                    }
                }
            }

            // --- Value Selector ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Target Value",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ValueSelector(
                        settingType = state.settingType,
                        currentValue = state.targetValue,
                        onValueChanged = onTargetValueChanged
                    )
                }
            }

            // --- Save Button ---
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    if (isEditing) Icons.Filled.Edit else Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Schedule" else "Create Schedule",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingTypeOption(
    type: SettingType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (type) {
        SettingType.REFRESH_RATE -> Icons.Filled.Speed
        SettingType.WIFI -> Icons.Filled.Wifi
        SettingType.BLUETOOTH -> Icons.Filled.Bluetooth
        SettingType.DO_NOT_DISTURB -> Icons.Filled.DoNotDisturb
        SettingType.BRIGHTNESS -> Icons.Filled.LightMode
    }

    val label = when (type) {
        SettingType.REFRESH_RATE -> "Refresh Rate"
        SettingType.WIFI -> "WiFi"
        SettingType.BLUETOOTH -> "Bluetooth"
        SettingType.DO_NOT_DISTURB -> "Do Not Disturb"
        SettingType.BRIGHTNESS -> "Brightness"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ValueSelector(
    settingType: SettingType,
    currentValue: String,
    onValueChanged: (String) -> Unit
) {
    when (settingType) {
        SettingType.REFRESH_RATE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("60", "120").forEach { rate ->
                    FilterChip(
                        selected = currentValue == rate,
                        onClick = { onValueChanged(rate) },
                        label = { Text("${rate}Hz") },
                        leadingIcon = if (currentValue == rate) {
                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        SettingType.WIFI, SettingType.BLUETOOTH, SettingType.DO_NOT_DISTURB -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val onLabel = when (settingType) {
                    SettingType.DO_NOT_DISTURB -> "Enable"
                    else -> "Turn On"
                }
                val offLabel = when (settingType) {
                    SettingType.DO_NOT_DISTURB -> "Disable"
                    else -> "Turn Off"
                }

                FilterChip(
                    selected = currentValue == "true",
                    onClick = { onValueChanged("true") },
                    label = { Text(onLabel) },
                    leadingIcon = if (currentValue == "true") {
                        { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = currentValue == "false",
                    onClick = { onValueChanged("false") },
                    label = { Text(offLabel) },
                    leadingIcon = if (currentValue == "false") {
                        { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SettingType.BRIGHTNESS -> {
            val brightnessValue = currentValue.toFloatOrNull() ?: 128f
            val percentage = (brightnessValue / 255f * 100).toInt()

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Brightness Level",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = brightnessValue,
                    onValueChange = { onValueChanged(it.toInt().toString()) },
                    valueRange = 0f..255f,
                    steps = 0
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
