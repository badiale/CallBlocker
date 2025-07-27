package dev.badiale.callblocker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun PermissionRequestUI(
    @PreviewParameter(PermissionNameParameterProvider::class) permissionName: String,
    description: String = "",
    isGranted: State<Boolean> = mutableStateOf(false),
    requestPermission: () -> Unit = {}
) {
    val permissionGranted by isGranted

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .toggleable(
                role = Role.Checkbox,
                value = permissionGranted,
                onValueChange = { requestPermission() }
            )
    ) {
        Checkbox(checked = permissionGranted, onCheckedChange = null)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = permissionName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description, modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private class PermissionNameParameterProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("CAMERA")
}