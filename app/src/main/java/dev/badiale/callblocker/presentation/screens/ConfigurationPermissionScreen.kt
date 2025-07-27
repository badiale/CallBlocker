package dev.badiale.callblocker.presentation.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.badiale.callblocker.presentation.components.PermissionRequestUI
import dev.badiale.callblocker.presentation.components.PermissionUI
import dev.badiale.callblocker.presentation.components.RoleUI
import kotlinx.coroutines.flow.MutableStateFlow

@Preview
@Composable
fun ConfigurationPermissionScreen() {
    val tabs = listOf("Permissions", "Roles", "System")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> PermissionsTab()
            1 -> RolesTab()
            2 -> SystemTab()
        }
    }

}

@Composable
fun PermissionsTab() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PermissionUI(
            permissionName = "READ_CONTACTS",
            permission = Manifest.permission.READ_CONTACTS
        )
        PermissionUI(
            permissionName = "READ_PHONE_STATE",
            permission = Manifest.permission.READ_PHONE_STATE
        )
        PermissionUI(
            permissionName = "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
            permission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        )
    }
}

@Composable
fun RolesTab() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleUI(
                roleName = "ROLE_CALL_SCREENING",
                role = RoleManager.ROLE_CALL_SCREENING,
            )
        }
    }
}

@Composable
@SuppressLint("BatteryLife")
fun SystemTab() {
    val context = LocalContext.current
    val powerManager = context.getSystemService(PowerManager::class.java) as PowerManager
    val permissionGranted =
        MutableStateFlow(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permissionGranted.value = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PermissionRequestUI(
            permissionName = "Battery Optimization Exception",
            isGranted = permissionGranted.collectAsState(),
            requestPermission = {
                val intent =
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = "package:${context.packageName}".toUri()
                permissionLauncher.launch(intent)
            }
        )
    }
}
