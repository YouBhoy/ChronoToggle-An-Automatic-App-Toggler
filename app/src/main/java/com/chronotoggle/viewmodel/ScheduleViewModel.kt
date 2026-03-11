package com.chronotoggle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chronotoggle.data.db.AppDatabase
import com.chronotoggle.data.model.Schedule
import com.chronotoggle.data.model.SettingType
import com.chronotoggle.data.repository.ScheduleRepository
import com.chronotoggle.scheduler.ScheduleAlarmManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScheduleEditorState(
    val id: Long? = null,
    val hour: Int = 8,
    val minute: Int = 0,
    val settingType: SettingType = SettingType.REFRESH_RATE,
    val targetValue: String = "60",
    val label: String = "",
    val isEnabled: Boolean = true
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScheduleRepository

    val allSchedules: StateFlow<List<Schedule>>

    private val _editorState = MutableStateFlow(ScheduleEditorState())
    val editorState: StateFlow<ScheduleEditorState> = _editorState.asStateFlow()

    init {
        val dao = AppDatabase.getInstance(application).scheduleDao()
        repository = ScheduleRepository(dao)
        allSchedules = repository.allSchedules
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Editor actions ---

    fun resetEditor() {
        _editorState.value = ScheduleEditorState()
    }

    fun loadScheduleForEdit(scheduleId: Long) {
        viewModelScope.launch {
            val schedule = repository.getById(scheduleId)
            if (schedule != null) {
                _editorState.value = ScheduleEditorState(
                    id = schedule.id,
                    hour = schedule.hour,
                    minute = schedule.minute,
                    settingType = schedule.settingType,
                    targetValue = schedule.targetValue,
                    label = schedule.label,
                    isEnabled = schedule.isEnabled
                )
            }
        }
    }

    fun updateEditorHour(hour: Int) {
        _editorState.update { it.copy(hour = hour) }
    }

    fun updateEditorMinute(minute: Int) {
        _editorState.update { it.copy(minute = minute) }
    }

    fun updateEditorSettingType(type: SettingType) {
        val defaultValue = when (type) {
            SettingType.REFRESH_RATE -> "60"
            SettingType.WIFI -> "true"
            SettingType.BLUETOOTH -> "true"
            SettingType.DO_NOT_DISTURB -> "true"
            SettingType.BRIGHTNESS -> "128"
        }
        _editorState.update { it.copy(settingType = type, targetValue = defaultValue) }
    }

    fun updateEditorTargetValue(value: String) {
        _editorState.update { it.copy(targetValue = value) }
    }

    fun updateEditorLabel(label: String) {
        _editorState.update { it.copy(label = label) }
    }

    // --- CRUD operations ---

    fun saveSchedule(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _editorState.value
            val schedule = Schedule(
                id = state.id ?: 0,
                hour = state.hour,
                minute = state.minute,
                settingType = state.settingType,
                targetValue = state.targetValue,
                isEnabled = state.isEnabled,
                label = state.label
            )

            val id = if (state.id != null) {
                repository.update(schedule)
                state.id
            } else {
                repository.insert(schedule)
            }

            // Schedule/update the alarm
            val saved = repository.getById(id) ?: schedule.copy(id = id)
            ScheduleAlarmManager.scheduleAlarm(getApplication(), saved)

            onComplete()
        }
    }

    fun toggleScheduleEnabled(schedule: Schedule) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = !schedule.isEnabled)
            repository.update(updated)
            if (updated.isEnabled) {
                ScheduleAlarmManager.scheduleAlarm(getApplication(), updated)
            } else {
                ScheduleAlarmManager.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            ScheduleAlarmManager.cancelAlarm(getApplication(), schedule.id)
            repository.delete(schedule)
        }
    }
}
