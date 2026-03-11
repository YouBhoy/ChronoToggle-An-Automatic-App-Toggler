package com.chronotoggle.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.chronotoggle.data.db.AppDatabase
import com.chronotoggle.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives alarm broadcasts when a scheduled time arrives.
 * Executes the setting change, then re-schedules the alarm for the next day.
 */
class ScheduleReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        private const val TAG = "ScheduleReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        if (scheduleId == -1L) {
            Log.w(TAG, "Received broadcast with no schedule ID")
            return
        }

        Log.d(TAG, "Alarm fired for schedule #$scheduleId")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = ScheduleRepository(db.scheduleDao())
                val schedule = repo.getById(scheduleId)

                if (schedule != null && schedule.isEnabled) {
                    SettingsExecutor.execute(context, schedule)
                    // Re-schedule for next day (daily repeating)
                    ScheduleAlarmManager.scheduleAlarm(context, schedule)
                } else {
                    Log.d(TAG, "Schedule #$scheduleId not found or disabled, skipping")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing schedule #$scheduleId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
