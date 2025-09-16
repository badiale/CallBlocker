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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.badiale.callblocker.R
import dev.badiale.callblocker.presentation.components.PermissionRequestUI
import dev.badiale.callblocker.presentation.components.PermissionUI
import dev.badiale.callblocker.presentation.components.RoleUI
import dev.badiale.callblocker.services.PreferenceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
object ConfigurationPermissionNavigation

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConfigurationPermissionScreen() {
    val preferenceService = PreferenceService(LocalContext.current)
    if (preferenceService.isFirstRun()) {
        val openDialog = remember { mutableStateOf(true) }

        if (openDialog.value) {
            BasicAlertDialog(
                onDismissRequest = {}
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = stringResource(R.string.first_run_title)
                        )
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            text = stringResource(R.string.first_run_body)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        TextButton(
                            onClick = {
                                openDialog.value = false
                                preferenceService.doFirstRun()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PermissionsTab()
        Spacer(Modifier.height(4.dp))

        RolesTab()
        Spacer(Modifier.height(4.dp))

        SystemTab()
    }

}

@Composable
fun PermissionsTab() {
    Text(
        text = stringResource(R.string.permissions),
        style = MaterialTheme.typography.titleMedium
    )
    PermissionUI(
        permissionName = stringResource(R.string.read_contacts_permission),
        description = stringResource(R.string.read_contacts_permission_description),
        permission = Manifest.permission.READ_CONTACTS
    )
    HorizontalDivider()
    PermissionUI(
        permissionName = stringResource(R.string.request_ignore_battery_optimizations_permission),
        description = stringResource(R.string.request_ignore_battery_optimizations_permission_description),
        permission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    )
}

@Composable
fun RolesTab() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    Text(
        text = stringResource(R.string.roles),
        style = MaterialTheme.typography.titleMedium
    )

    RoleUI(
        roleName = stringResource(R.string.role_call_screening_permission),
        description = stringResource(R.string.role_call_screening_permission_description),
        role = RoleManager.ROLE_CALL_SCREENING,
    )
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

    Text(
        text = stringResource(R.string.system),
        style = MaterialTheme.typography.titleMedium
    )
    PermissionRequestUI(
        permissionName = stringResource(R.string.action_request_ignore_battery_optimizations),
        description = stringResource(R.string.action_request_ignore_battery_optimizations_description),
        isGranted = permissionGranted.collectAsState(),
        requestPermission = {
            val intent =
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = "package:${context.packageName}".toUri()
            permissionLauncher.launch(intent)
        }
    )
}
