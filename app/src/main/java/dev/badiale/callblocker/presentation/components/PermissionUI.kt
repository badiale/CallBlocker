package dev.badiale.callblocker.presentation.components

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun PermissionUI(
    permissionName: String,
    description: String = "",
    permission: String
) {
    val context = LocalContext.current
    val permissionGranted = MutableStateFlow(hasPermissions(context, permission))
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted.value = isGranted
    }

    PermissionRequestUI(
        permissionName = permissionName,
        description = description,
        isGranted = permissionGranted.collectAsState(),
        requestPermission = { permissionLauncher.launch(permission) })
}

private fun hasPermissions(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED