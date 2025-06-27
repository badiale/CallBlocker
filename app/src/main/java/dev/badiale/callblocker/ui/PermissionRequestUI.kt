package dev.badiale.callblocker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionRequestUI(
    permissionName: String,
    description: String = "",
    isGranted: () -> Boolean,
    requestPermission: () -> Unit
) {
    var permissionGranted by remember { mutableStateOf(isGranted()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = permissionName)
        Text(text = if (permissionGranted) "✅ Granted" else "❌ Not granted")
        Text(text = description, modifier = Modifier.padding(top = 4.dp))
        Button(onClick = {
            requestPermission()
            permissionGranted = isGranted()
        }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Request $permissionName Permission")
        }
    }
}