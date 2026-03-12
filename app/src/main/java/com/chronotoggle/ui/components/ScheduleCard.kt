package com.chronotoggle.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronotoggle.data.model.Schedule
import com.chronotoggle.data.model.SettingType

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onToggle: (Schedule) -> Unit,
    onEdit: (Schedule) -> Unit,
    onDelete: (Schedule) -> Unit,
    onRunNow: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardColors = if (schedule.isEnabled) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = cardColors,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Setting icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (schedule.isEnabled) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSettingIcon(schedule.settingType),
                    contentDescription = null,
                    tint = if (schedule.isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Schedule info
            Column(modifier = Modifier.weight(1f)) {
                if (schedule.label.isNotBlank()) {
                    Text(
                        text = schedule.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = formatTime(schedule.hour, schedule.minute),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (schedule.isEnabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatSettingDescription(schedule),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = { onToggle(schedule) }
                )

                Row {
                    IconButton(
                        onClick = { onRunNow(schedule) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Run Now",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onEdit(schedule) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Schedule") },
            text = { Text("Are you sure you want to delete this schedule?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(schedule)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, period)
}

private fun formatSettingDescription(schedule: Schedule): String {
    return when (schedule.settingType) {
        SettingType.REFRESH_RATE -> "Refresh Rate → ${schedule.targetValue}Hz"
        SettingType.WIFI -> "WiFi → ${if (schedule.targetValue == "true") "On" else "Off"}"
        SettingType.BLUETOOTH -> "Bluetooth → ${if (schedule.targetValue == "true") "On" else "Off"}"
        SettingType.DO_NOT_DISTURB -> "Do Not Disturb → ${if (schedule.targetValue == "true") "On" else "Off"}"
        SettingType.BRIGHTNESS -> "Brightness → ${((schedule.targetValue.toIntOrNull() ?: 0) * 100 / 255)}%"
    }
}

private fun getSettingIcon(type: SettingType): ImageVector {
    return when (type) {
        SettingType.REFRESH_RATE -> Icons.Filled.Speed
        SettingType.WIFI -> Icons.Filled.Wifi
        SettingType.BLUETOOTH -> Icons.Filled.Bluetooth
        SettingType.DO_NOT_DISTURB -> Icons.Filled.DoNotDisturb
        SettingType.BRIGHTNESS -> Icons.Filled.LightMode
    }
}
