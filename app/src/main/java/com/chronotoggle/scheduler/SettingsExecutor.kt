package com.chronotoggle.scheduler

import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothManager
import android.net.wifi.WifiManager
import android.app.NotificationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.chronotoggle.data.model.Schedule
import com.chronotoggle.data.model.SettingType

/**
 * Executes the actual system setting changes for a given schedule.
 * Returns a human-readable result message and shows a Toast.
 */
object SettingsExecutor {

    private const val TAG = "SettingsExecutor"

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    fun execute(context: Context, schedule: Schedule): String {
        Log.d(TAG, "Executing schedule #${schedule.id}: ${schedule.settingType} → ${schedule.targetValue}")
        val result = try {
            when (schedule.settingType) {
                SettingType.REFRESH_RATE -> setRefreshRate(context, schedule.targetValue)
                SettingType.WIFI -> setWifi(context, schedule.targetValue.toBoolean())
                SettingType.BLUETOOTH -> setBluetooth(context, schedule.targetValue.toBoolean())
                SettingType.DO_NOT_DISTURB -> setDoNotDisturb(context, schedule.targetValue.toBoolean())
                SettingType.BRIGHTNESS -> setBrightness(context, schedule.targetValue.toIntOrNull() ?: 128)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute schedule #${schedule.id}: ${e.message}", e)
            "❌ Failed: ${e.message}"
        }
        showToast(context, result)
        return result
    }

    // --- Refresh Rate ---

    private fun setRefreshRate(context: Context, value: String): String {
        val rate = value.toFloatOrNull() ?: 60f
        val rateInt = rate.toInt()
        var success = false

        // Write to all known refresh rate keys across OEMs
        // 1. Settings.Secure — standard AOSP (peak_refresh_rate / min_refresh_rate)
        try {
            Settings.Secure.putFloat(context.contentResolver, "peak_refresh_rate", rate)
            Settings.Secure.putFloat(context.contentResolver, "min_refresh_rate", rate)
            Log.d(TAG, "Wrote Secure peak/min_refresh_rate = $rate")
            success = true
        } catch (e: Exception) {
            Log.w(TAG, "Settings.Secure peak/min failed: ${e.message}")
        }

        // 2. Settings.System — some OEMs use this instead
        try {
            if (Settings.System.canWrite(context)) {
                Settings.System.putFloat(context.contentResolver, "peak_refresh_rate", rate)
                Settings.System.putFloat(context.contentResolver, "min_refresh_rate", rate)
                // TECNO/Transsion-specific key
                Settings.System.putInt(context.contentResolver, "tran_refresh_mode", rateInt)
                Settings.System.putInt(context.contentResolver, "last_tran_refresh_mode_in_refresh_setting", rateInt)
                Log.d(TAG, "Wrote System refresh rate keys = $rateInt")
                success = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Settings.System refresh rate failed: ${e.message}")
        }

        // 3. Settings.Global — a few devices use this
        try {
            Settings.Global.putFloat(context.contentResolver, "peak_refresh_rate", rate)
            Settings.Global.putFloat(context.contentResolver, "min_refresh_rate", rate)
            Log.d(TAG, "Wrote Global peak/min_refresh_rate = $rate")
            success = true
        } catch (e: Exception) {
            Log.w(TAG, "Settings.Global refresh rate failed: ${e.message}")
        }

        return if (success) {
            "✅ Refresh rate → ${rateInt}Hz"
        } else {
            "❌ Refresh Rate: Could not write to any settings provider. Grant WRITE_SECURE_SETTINGS via ADB."
        }
    }

    // --- WiFi ---

    @Suppress("DEPRECATION")
    private fun setWifi(context: Context, enabled: Boolean): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ blocks programmatic WiFi toggle.
            // Open the WiFi settings panel for the user.
            try {
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(panelIntent)
                return "⚠️ WiFi: Android 10+ requires manual toggle. Opening WiFi panel…"
            } catch (e: Exception) {
                Log.w(TAG, "WiFi panel failed: ${e.message}")
                return "❌ WiFi: Cannot open WiFi panel on this device"
            }
        }
        wifiManager.isWifiEnabled = enabled
        Log.d(TAG, "WiFi set to $enabled")
        return "✅ WiFi → ${if (enabled) "ON" else "OFF"}"
    }

    // --- Bluetooth ---

    @Suppress("DEPRECATION", "MissingPermission")
    private fun setBluetooth(context: Context, enabled: Boolean): String {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        if (adapter == null) {
            Log.w(TAG, "Bluetooth adapter not available")
            return "❌ Bluetooth: Adapter not available on this device"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.w(TAG, "Bluetooth programmatic toggle not supported on Android 13+")
            try {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open Bluetooth settings: ${e.message}")
            }
            return "⚠️ Bluetooth: Can't toggle on Android 13+. Opening settings…"
        }
        try {
            if (enabled) adapter.enable() else adapter.disable()
            Log.d(TAG, "Bluetooth set to $enabled")
            return "✅ Bluetooth → ${if (enabled) "ON" else "OFF"}"
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth toggle failed: ${e.message}")
            return "❌ Bluetooth: ${e.message}"
        }
    }

    // --- Do Not Disturb ---

    private fun setDoNotDisturb(context: Context, enabled: Boolean): String {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "DND policy access not granted")
            return "❌ DND: Notification policy access not granted. Please enable it in app settings."
        }
        notificationManager.setInterruptionFilter(
            if (enabled) NotificationManager.INTERRUPTION_FILTER_NONE
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
        Log.d(TAG, "Do Not Disturb set to $enabled")
        return "✅ Do Not Disturb → ${if (enabled) "ON" else "OFF"}"
    }

    // --- Brightness ---

    private fun setBrightness(context: Context, level: Int): String {
        if (!Settings.System.canWrite(context)) {
            Log.w(TAG, "Cannot write system settings — permission not granted")
            return "❌ Brightness: 'Modify System Settings' permission not granted. Please enable it in app settings."
        }
        val clamped = level.coerceIn(0, 255)
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
        return "✅ Brightness → ${(clamped * 100 / 255)}%"
    }
}
