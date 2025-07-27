package dev.badiale.callblocker.presentation.screens

sealed class Screen(val route: String) {
    object CallLog : Screen("call-log")
    object ConfigurationPermission : Screen("configuration/permission")
}