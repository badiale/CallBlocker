package dev.badiale.callblocker.presentation.screens

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.badiale.callblocker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import androidx.core.content.edit
import dev.badiale.callblocker.services.PreferenceService

@Serializable
object ConfigurationNavigation

@Preview
@Composable
fun ConfigurationScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        GeneralTab()
        Spacer(Modifier.height(4.dp))
    }

}

@Composable
fun GeneralTab() {
    val context = LocalContext.current
    val preference = PreferenceService(context)
    val configurationEnabled = MutableStateFlow(preference.isBlockUnknownNumber())
    val initialValue by configurationEnabled.collectAsState()

    Text(
        text = stringResource(R.string.general),
        style = MaterialTheme.typography.titleMedium
    )

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .toggleable(
                role = Role.Checkbox,
                value = initialValue,
                onValueChange = {
                    preference.setBlockUnknownNumber(it)
                    configurationEnabled.value = it
                }
            )
    ) {
        Checkbox(checked = initialValue, onCheckedChange = null)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = stringResource(R.string.configuration_block_enabled),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.configuration_block_enabled_description),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}