package dev.badiale.callblocker.presentation.components

import android.app.role.RoleManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import dev.badiale.callblocker.utils.RequestRoleContract
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun RoleUI(
    roleName: String,
    description: String = "",
    role: String
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return;
    }
    val context = LocalContext.current
    val roleManager = context.getSystemService(RoleManager::class.java)
    val permissionGranted = MutableStateFlow(roleManager.isRoleHeld(role))
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestRoleContract()
    ) { isGranted ->
        permissionGranted.value = isGranted
    }

    PermissionRequestUI(
        permissionName = roleName,
        description = description,
        isGranted = permissionGranted.collectAsState(),
        requestPermission = { permissionLauncher.launch(role) })
}