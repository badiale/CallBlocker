package dev.badiale.callblocker.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.badiale.callblocker.R
import dev.badiale.callblocker.presentation.screens.Screen

@Composable
fun DrawerContent(onDestinationClicked: (Screen) -> Unit) {
    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.navigation),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider()
        ScreenDrawerItem(
            screen = Screen.CallLog,
            onDestinationClicked = onDestinationClicked
        )
        ScreenDrawerItem(
            screen = Screen.Configuration,
            onDestinationClicked = onDestinationClicked
        )
        ScreenDrawerItem(
            screen = Screen.ConfigurationPermission,
            onDestinationClicked = onDestinationClicked
        )
    }
}

@Composable
fun ScreenDrawerItem(screen: Screen, onDestinationClicked: (Screen) -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(screen.name)) },
        selected = false,
        onClick = { onDestinationClicked(screen) }
    )
}