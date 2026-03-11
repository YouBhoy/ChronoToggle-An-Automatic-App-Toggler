package com.chronotoggle.scheduler

import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothManager
import android.net.wifi.WifiManager
import android.app.NotificationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.chronotoggle.data.model.Schedule
import com.chronotoggle.data.model.SettingType

/**
 * Executes the actual system setting changes for a given schedule.
 */
object SettingsExecutor {

    private const val TAG = "SettingsExecutor"

    fun execute(context: Context, schedule: Schedule) {
        Log.d(TAG, "Executing schedule #${schedule.id}: ${schedule.settingType} → ${schedule.targetValue}")
        when (schedule.settingType) {
            SettingType.REFRESH_RATE -> setRefreshRate(context, schedule.targetValue)
            SettingType.WIFI -> setWifi(context, schedule.targetValue.toBooleanStrict())
            SettingType.BLUETOOTH -> setBluetooth(context, schedule.targetValue.toBooleanStrict())
            SettingType.DO_NOT_DISTURB -> setDoNotDisturb(context, schedule.targetValue.toBooleanStrict())
            SettingType.BRIGHTNESS -> setBrightness(context, schedule.targetValue.toInt())
        }
    }

    // --- Refresh Rate ---

    private fun setRefreshRate(context: Context, value: String) {
        // Refresh rate requires WRITE_SETTINGS permission.
        // We write the preferred peak refresh rate into Settings.System.
        // Note: actual effect depends on device OEM support.
        if (!Settings.System.canWrite(context)) {
            Log.w(TAG, "Cannot write system settings — permission not granted")
            return
        }
        val rate = value.toFloatOrNull() ?: 60f
        Settings.System.putFloat(
            context.contentResolver,
            "peak_refresh_rate",
            rate
        )
        Settings.System.putFloat(
            context.contentResolver,
            "min_refresh_rate",
            rate
        )
        Log.d(TAG, "Refresh rate set to ${rate}Hz")
    }

    // --- WiFi ---

    @Suppress("DEPRECATION")
    private fun setWifi(context: Context, enabled: Boolean) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        // WifiManager.setWifiEnabled is deprecated on API 29+ but still functional with CHANGE_WIFI_STATE.
        // On Android 10+ a settings panel intent is the official way, but we use the legacy API
        // for background automation where no UI is available.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            wifiManager.isWifiEnabled = enabled
        } else {
            // For API 29+, launch the WiFi settings panel — this only works if a UI is present.
            // In background we attempt the legacy call; some ROMs still allow it.
            try {
                wifiManager.isWifiEnabled = enabled
            } catch (e: Exception) {
                Log.w(TAG, "WiFi toggle failed on API 29+: ${e.message}")
            }
        }
        Log.d(TAG, "WiFi set to $enabled")
    }

    // --- Bluetooth ---

    @Suppress("DEPRECATION", "MissingPermission")
    private fun setBluetooth(context: Context, enabled: Boolean) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        if (adapter == null) {
            Log.w(TAG, "Bluetooth adapter not available")
            return
        }
        if (enabled) adapter.enable() else adapter.disable()
        Log.d(TAG, "Bluetooth set to $enabled")
    }

    // --- Do Not Disturb ---

    private fun setDoNotDisturb(context: Context, enabled: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "DND policy access not granted")
            return
        }
        notificationManager.setInterruptionFilter(
            if (enabled) NotificationManager.INTERRUPTION_FILTER_NONE
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
        Log.d(TAG, "Do Not Disturb set to $enabled")
    }

    // --- Brightness ---

    private fun setBrightness(context: Context, level: Int) {
        if (!Settings.System.canWrite(context)) {
            Log.w(TAG, "Cannot write system settings — permission not granted")
            return
        }
        val clamped = level.coerceIn(0, 255)
        // Disable auto-brightness first
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            clamped
        )
        Log.d(TAG, "Brightness set to $clamped")
    }
}
