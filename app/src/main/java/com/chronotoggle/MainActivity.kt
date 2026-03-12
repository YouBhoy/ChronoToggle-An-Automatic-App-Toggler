package com.chronotoggle

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.chronotoggle.ui.navigation.AppNavGraph
import com.chronotoggle.ui.theme.ChronoToggleTheme
import com.chronotoggle.viewmodel.ScheduleViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ScheduleViewModel

    /** Queue of permission intents to launch one at a time. */
    private val pendingPermissionIntents = mutableListOf<Intent>()

    private val permissionSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // When the user returns from a settings screen, launch the next one
        launchNextPermissionIntent()
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // After Bluetooth permission result, continue the queue
        launchNextPermissionIntent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[ScheduleViewModel::class.java]

        requestRequiredPermissions()

        setContent {
            ChronoToggleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        pendingPermissionIntents.clear()

        // System settings write permission (for brightness, refresh rate)
        if (!Settings.System.canWrite(this)) {
            pendingPermissionIntents.add(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }

        // Do Not Disturb policy access
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            pendingPermissionIntents.add(
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            )
        }

        // Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                pendingPermissionIntents.add(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }
        }

        // Start the chain — Bluetooth is handled separately below since it uses the runtime dialog
        launchNextPermissionIntent()
    }

    private fun launchNextPermissionIntent() {
        if (pendingPermissionIntents.isNotEmpty()) {
            val next = pendingPermissionIntents.removeAt(0)
            permissionSettingsLauncher.launch(next)
        } else {
            // All settings-style permissions done — now request Bluetooth (runtime dialog)
            requestBluetoothIfNeeded()
        }
    }

    private fun requestBluetoothIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
    }
}
