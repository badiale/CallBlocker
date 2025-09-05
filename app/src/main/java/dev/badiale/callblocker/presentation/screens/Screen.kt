package dev.badiale.callblocker.presentation.screens

import androidx.annotation.StringRes
import dev.badiale.callblocker.R

sealed class Screen(val route: Any, @StringRes val name: Int) {
    object CallLog : Screen(CallLogScreenNavigation, R.string.call_log)
    object Configuration : Screen(ConfigurationNavigation, R.string.configuration)
    object ConfigurationPermission : Screen(
        ConfigurationPermissionNavigation, R.string.permissions
    )
}