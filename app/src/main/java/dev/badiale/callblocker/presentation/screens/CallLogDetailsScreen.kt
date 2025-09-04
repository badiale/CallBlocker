package dev.badiale.callblocker.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.badiale.callblocker.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class CallLogDetailsNavigation(val id: Int)

@Composable
fun CallLogDetailsScreen(logId: Int) {
    val context = LocalContext.current
    val callLogRepository = CallLogRepository(context)
    val callLog = MutableStateFlow<Map<String, String>>(emptyMap())
    val logs by callLog.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            return@LaunchedEffect
        }
        callLog.value = callLogRepository.findById(logId)
    }

    LazyColumn(state = listState) {
        if (logs.isEmpty()) {
            item {
                Text(
                    text = "Call not found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(logs.entries.toList().sortedBy { it.key }) { entry ->
                Row(
                    Modifier
                        .fillParentMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .weight(0.5f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        text = entry.key,
                    )
                    Text(
                        modifier = Modifier
                            .weight(0.5f),
                        style = MaterialTheme.typography.bodyLarge,
                        text = entry.value,
                    )
                }
                HorizontalDivider()
            }
        }
    }
}