package dev.badiale.callblocker.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.badiale.callblocker.presentation.screens.CallLogScreen
import dev.badiale.callblocker.presentation.screens.ConfigurationPermissionScreen
import dev.badiale.callblocker.presentation.screens.Screen

fun NavGraphBuilder.appNavGraph(navController: NavController) {
    composable(Screen.CallLog.route) {
        CallLogScreen()
    }
    composable(Screen.ConfigurationPermission.route) {
        ConfigurationPermissionScreen()
    }
}