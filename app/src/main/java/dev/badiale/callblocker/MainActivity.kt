package dev.badiale.callblocker

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.badiale.callblocker.ui.PermissionRequestUI
import dev.badiale.callblocker.ui.PermissionUI
import dev.badiale.callblocker.ui.RoleUI
import dev.badiale.callblocker.ui.theme.CallBlockerTheme

class MainActivity : ComponentActivity() {
    private lateinit var roleRequestLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roleRequestLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Toast.makeText(this, "$it", Toast.LENGTH_LONG).show()
            }

        enableEdgeToEdge()
        setContent {
            val tabs = listOf("Permissions", "Roles", "System")
            var selectedTabIndex by remember { mutableIntStateOf(0) }

            CallBlockerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
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
                            1 -> RolesTab(roleRequestLauncher)
                            2 -> SystemTab()
                        }
                    }
                }
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
    fun RolesTab(roleRequestLauncher: ActivityResultLauncher<Intent>) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleUI(
                    roleName = "ROLE_DIALER",
                    role = RoleManager.ROLE_DIALER,
                    roleRequestLauncher = roleRequestLauncher
                )
                RoleUI(
                    roleName = "ROLE_CALL_SCREENING",
                    role = RoleManager.ROLE_CALL_SCREENING,
                    roleRequestLauncher = roleRequestLauncher
                )
                RoleUI(
                    roleName = "ROLE_CALL_REDIRECTION",
                    role = RoleManager.ROLE_CALL_REDIRECTION,
                    roleRequestLauncher = roleRequestLauncher
                )
            }

        }
    }

    @Composable
    @SuppressLint("BatteryLife")
    fun SystemTab() {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PermissionRequestUI(
                permissionName = "Battery Optimization Exception",
                isGranted = {
                    val powerManager =
                        getSystemService(Context.POWER_SERVICE) as PowerManager
                    powerManager.isIgnoringBatteryOptimizations(packageName)
                },
                requestPermission = {
                    val intent =
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = "package:$packageName".toUri()
                    startActivity(intent)
                }
            )
        }
    }
}