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
 * Re-schedules all enabled alarms after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed — rescheduling all enabled alarms")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = ScheduleRepository(db.scheduleDao())
                val enabledSchedules = repo.getEnabled()

                enabledSchedules.forEach { schedule ->
                    ScheduleAlarmManager.scheduleAlarm(context, schedule)
                }

                Log.d(TAG, "Re-scheduled ${enabledSchedules.size} alarms after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
