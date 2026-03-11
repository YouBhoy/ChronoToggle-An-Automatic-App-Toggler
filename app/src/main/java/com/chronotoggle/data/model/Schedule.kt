package com.chronotoggle.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the type of system setting a schedule can control.
 */
enum class SettingType {
    REFRESH_RATE,
    WIFI,
    BLUETOOTH,
    DO_NOT_DISTURB,
    BRIGHTNESS
}

/**
 * A single scheduled automation rule.
 *
 * @param id Auto-generated primary key.
 * @param hour Hour of day (0-23) when this schedule fires.
 * @param minute Minute (0-59) when this schedule fires.
 * @param settingType Which system setting to modify.
 * @param targetValue The value to set — interpretation depends on [settingType]:
 *   - REFRESH_RATE: "60" or "120" (Hz)
 *   - WIFI / BLUETOOTH / DO_NOT_DISTURB: "true" or "false"
 *   - BRIGHTNESS: "0" to "255"
 * @param isEnabled Whether this schedule is active.
 * @param label Optional user-friendly label.
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val settingType: SettingType,
    val targetValue: String,
    val isEnabled: Boolean = true,
    val label: String = ""
)
