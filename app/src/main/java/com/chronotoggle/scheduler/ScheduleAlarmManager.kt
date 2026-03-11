package com.chronotoggle.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.chronotoggle.data.model.Schedule
import java.util.Calendar

/**
 * Manages AlarmManager-based scheduling for each [Schedule].
 * Each schedule is identified by its database ID as the alarm request code.
 */
object ScheduleAlarmManager {

    private const val TAG = "ScheduleAlarmManager"

    fun scheduleAlarm(context: Context, schedule: Schedule) {
        if (!schedule.isEnabled) {
            cancelAlarm(context, schedule.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduleReceiver::class.java).apply {
            putExtra(ScheduleReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(schedule.hour, schedule.minute)

        // Use setExactAndAllowWhileIdle for reliable execution even in Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                Log.w(TAG, "Exact alarm permission not granted, falling back to inexact")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Log.d(TAG, "Scheduled alarm for #${schedule.id} at ${schedule.hour}:${schedule.minute} → trigger=$triggerTime")
    }

    fun cancelAlarm(context: Context, scheduleId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduleReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm for #$scheduleId")
    }

    /**
     * Returns the next occurrence of [hour]:[minute] in millis.
     * If the time has already passed today, returns tomorrow's occurrence.
     */
    private fun getNextTriggerTime(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If the time already passed today, schedule for tomorrow
        if (trigger.before(now) || trigger == now) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }
}
