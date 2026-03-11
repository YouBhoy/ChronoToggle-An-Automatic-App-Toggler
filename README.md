# ChronoToggle — Automatic Phone Settings Toggler

An Android app that **automatically toggles phone settings at specific times of the day**. Set rules like switching to 60Hz at night and back to 120Hz in the morning — all running in the background.

---

## Features

- **Schedule-based automation** — Create rules that fire at exact times daily
- **Supported settings:**
  - Screen refresh rate (60Hz / 120Hz)
  - WiFi on/off
  - Bluetooth on/off
  - Do Not Disturb on/off
  - Screen brightness level
- **Material Design 3 UI** with Jetpack Compose
- **Dark mode** — follows system theme (+ dynamic colors on Android 12+)
- **Persistent schedules** — stored in Room database
- **Background execution** — AlarmManager fires even when app is closed
- **Survives reboot** — BootReceiver re-schedules all alarms on device restart
- **Edit & delete** schedules with confirmation dialogs

---

## Architecture

```
MVVM + Repository Pattern
├── UI Layer (Jetpack Compose)
│   ├── Screens (Home, Editor)
│   ├── Components (ScheduleCard)
│   ├── Navigation (NavHost)
│   └── Theme (Material3)
├── ViewModel Layer
│   └── ScheduleViewModel
├── Data Layer
│   ├── Repository (ScheduleRepository)
│   ├── Database (Room — AppDatabase, ScheduleDao)
│   └── Model (Schedule, SettingType)
└── Scheduler Layer
    ├── ScheduleAlarmManager (alarm setup/cancel)
    ├── ScheduleReceiver (executes on alarm)
    ├── BootReceiver (reschedules after reboot)
    └── SettingsExecutor (applies system changes)
```

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/chronotoggle/
│   ├── ChronoToggleApp.kt          # Application class
│   ├── MainActivity.kt             # Entry point + permission handling
│   ├── data/
│   │   ├── model/
│   │   │   └── Schedule.kt         # Data model + SettingType enum
│   │   ├── db/
│   │   │   ├── AppDatabase.kt      # Room database singleton
│   │   │   ├── ScheduleDao.kt      # Data access object
│   │   │   └── Converters.kt       # Room type converters
│   │   └── repository/
│   │       └── ScheduleRepository.kt
│   ├── scheduler/
│   │   ├── ScheduleAlarmManager.kt  # Schedule/cancel exact alarms
│   │   ├── ScheduleReceiver.kt      # BroadcastReceiver for alarm events
│   │   ├── BootReceiver.kt          # Re-schedule alarms after reboot
│   │   └── SettingsExecutor.kt      # Execute system setting changes
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   ├── navigation/
│   │   │   ├── Screen.kt
│   │   │   └── AppNavGraph.kt
│   │   ├── screens/
│   │   │   ├── HomeScreen.kt       # Main schedule list
│   │   │   └── ScheduleEditorScreen.kt  # Create/edit schedule
│   │   └── components/
│   │       └── ScheduleCard.kt      # Individual schedule card
│   └── viewmodel/
│       └── ScheduleViewModel.kt
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   ├── themes.xml
    │   └── ic_launcher_background.xml
    └── mipmap-anydpi-v26/
        ├── ic_launcher.xml
        ├── ic_launcher_round.xml
        └── ic_launcher_foreground.xml
```

---

## How to Run

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17**
- Android SDK with **API 34** (Android 14) installed
- An **Android device or emulator** running API 26+ (Android 8.0+)

### Steps

1. **Clone or copy** this project folder.

2. **Open in Android Studio:**
   - File → Open → select the `ChronoToggle-An-Automatic-App-Toggler` folder
   - Wait for Gradle sync to complete

3. **Run the app:**
   - Select your device/emulator from the toolbar
   - Click **Run ▶** (or `Shift+F10`)

4. **Grant permissions** when prompted:
   - **Modify System Settings** — required for brightness and refresh rate
   - **Do Not Disturb access** — required for DND toggling
   - **Bluetooth** — required for Bluetooth control
   - **Exact Alarms** — required for precise scheduling

### First Use

1. Tap **"New Schedule"** on the home screen
2. Enter an optional label (e.g., "Night Mode")
3. Pick a time using the time picker
4. Select which setting to change (refresh rate, WiFi, etc.)
5. Choose the target value
6. Tap **"Create Schedule"**
7. The schedule appears on the home screen with a toggle switch

---

## Permissions Explained

| Permission | Why |
|---|---|
| `WRITE_SETTINGS` | Change brightness and refresh rate |
| `ACCESS_NOTIFICATION_POLICY` | Toggle Do Not Disturb mode |
| `CHANGE_WIFI_STATE` | Turn WiFi on/off |
| `BLUETOOTH_CONNECT` | Turn Bluetooth on/off (Android 12+) |
| `SCHEDULE_EXACT_ALARM` | Fire alarms at exact times |
| `RECEIVE_BOOT_COMPLETED` | Re-register alarms after device reboot |
| `WAKE_LOCK` | Keep CPU awake during setting execution |

---

## Technical Notes

- **Refresh rate** control writes `peak_refresh_rate` and `min_refresh_rate` to `Settings.System`. Effectiveness varies by device OEM.
- **WiFi toggle** on Android 10+ uses the deprecated `WifiManager.setWifiEnabled()` — some ROMs still support it in the background. The official approach requires a UI settings panel.
- **Alarms** use `setExactAndAllowWhileIdle()` for Doze-safe execution. Each alarm re-schedules itself for the next day after firing — effectively creating a daily repeating schedule.
- **Room database** stores all schedules persistently. The DAO exposes a `Flow<List<Schedule>>` for reactive UI updates.
- **Dynamic colors** (Material You) are enabled on Android 12+ devices, falling back to the custom blue color scheme on older devices.

---

## Extending the App

The modular architecture makes it easy to add new features:

- **New setting types:** Add a value to `SettingType` enum, implement the logic in `SettingsExecutor`, and add UI options in `ScheduleEditorScreen`
- **Day-of-week rules:** Add a `daysOfWeek` field to `Schedule` and filter in `ScheduleReceiver`
- **Profiles:** Group multiple settings into a single "profile" schedule
- **Notification on execution:** Add a notification channel in `ScheduleReceiver`
- **Widget:** Create an app widget showing upcoming schedules

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Database | Room |
| Scheduling | AlarmManager + BroadcastReceiver |
| Navigation | Navigation Compose |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
