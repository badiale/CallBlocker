package dev.badiale.callblocker.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.badiale.callblocker.R
import dev.badiale.callblocker.presentation.components.DrawerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onDestinationClicked = { screen ->
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(screen.route)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = CallLogScreenNavigation,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<CallLogScreenNavigation> {
                    CallLogScreen(navController)
                }
                composable<CallLogDetailsNavigation> {
                    val details = it.toRoute<CallLogDetailsNavigation>()
                    CallLogDetailsScreen(logId = details.id)
                }
                composable<ConfigurationPermissionNavigation> {
                    ConfigurationPermissionScreen()
                }
                composable<ConfigurationNavigation> {
                    ConfigurationScreen()
                }
            }
        }
    }
}
