package dev.badiale.callblocker.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRequestUI(
    permissionName: String,
    description: String = "",
    isGranted: State<Boolean>,
    requestPermission: () -> Unit
) {
    val permissionGranted by isGranted

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = permissionName)
        Text(text = if (permissionGranted) "✅ Granted" else "❌ Not granted")
        Text(text = description, modifier = Modifier.padding(top = 4.dp))
        Button(onClick = {
            requestPermission()
        }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Request $permissionName Permission")
        }
    }
}