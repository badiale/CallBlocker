package dev.badiale.callblocker.presentation.screens

import androidx.annotation.StringRes
import dev.badiale.callblocker.R

sealed class Screen(val route: String, @StringRes val name: Int) {
    object CallLog : Screen("call-log", R.string.call_log)
    object ConfigurationPermission : Screen("configuration/permission", R.string.permissions)
}