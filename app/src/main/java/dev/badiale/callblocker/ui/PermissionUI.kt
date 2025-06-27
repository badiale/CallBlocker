package dev.badiale.callblocker.ui

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun PermissionUI(
    permissionName: String,
    description: String = "",
    permission: String
) {
    val context = LocalContext.current

    PermissionRequestUI(
        permissionName = permissionName,
        description = description,
        isGranted = { hasPermissions(context, permission) },
        requestPermission = { requestPermissions(context, permission) })
}

private fun hasPermissions(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

private fun requestPermissions(context: Context, permission: String) {
    ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(permission),
        1
    )
}