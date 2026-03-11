package com.chronotoggle.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chronotoggle.ui.screens.HomeScreen
import com.chronotoggle.ui.screens.ScheduleEditorScreen
import com.chronotoggle.viewmodel.ScheduleViewModel

@Composable
fun AppNavGraph(viewModel: ScheduleViewModel) {
    val navController = rememberNavController()
    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                schedules = schedules,
                onAddSchedule = {
                    viewModel.resetEditor()
                    navController.navigate(Screen.Editor.createRoute())
                },
                onEditSchedule = { schedule ->
                    viewModel.loadScheduleForEdit(schedule.id)
                    navController.navigate(Screen.Editor.createRoute(schedule.id))
                },
                onToggleSchedule = { schedule ->
                    viewModel.toggleScheduleEnabled(schedule)
                },
                onDeleteSchedule = { schedule ->
                    viewModel.deleteSchedule(schedule)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("scheduleId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: -1L
            val isEditing = scheduleId != -1L

            ScheduleEditorScreen(
                state = editorState,
                isEditing = isEditing,
                onHourChanged = viewModel::updateEditorHour,
                onMinuteChanged = viewModel::updateEditorMinute,
                onSettingTypeChanged = viewModel::updateEditorSettingType,
                onTargetValueChanged = viewModel::updateEditorTargetValue,
                onLabelChanged = viewModel::updateEditorLabel,
                onSave = {
                    viewModel.saveSchedule {
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
