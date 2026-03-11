package com.chronotoggle.ui.navigation

/**
 * Navigation route definitions for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Editor : Screen("editor?scheduleId={scheduleId}") {
        fun createRoute(scheduleId: Long? = null): String {
            return if (scheduleId != null) "editor?scheduleId=$scheduleId"
            else "editor"
        }
    }
}
