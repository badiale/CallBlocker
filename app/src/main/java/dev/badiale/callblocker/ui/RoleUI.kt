package dev.badiale.callblocker.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun RoleUI(
    roleName: String,
    description: String = "",
    role: String,
    roleRequestLauncher: ActivityResultLauncher<Intent>
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return;
    }
    val context = LocalContext.current
    val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager

    PermissionRequestUI(
        permissionName = roleName,
        description = description,
        isGranted = {
            roleManager.isRoleHeld(role)
        },
        requestPermission = {
            roleRequestLauncher.launch(roleManager.createRequestRoleIntent(role))
        })
}