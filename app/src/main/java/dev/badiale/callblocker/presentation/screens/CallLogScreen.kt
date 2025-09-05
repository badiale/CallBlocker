package dev.badiale.callblocker.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog.Calls.ANSWERED_EXTERNALLY_TYPE
import android.provider.CallLog.Calls.BLOCKED_TYPE
import android.provider.CallLog.Calls.INCOMING_TYPE
import android.provider.CallLog.Calls.MISSED_TYPE
import android.provider.CallLog.Calls.OUTGOING_TYPE
import android.provider.CallLog.Calls.REJECTED_TYPE
import android.provider.CallLog.Calls.VOICEMAIL_TYPE
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.badiale.callblocker.R
import dev.badiale.callblocker.domain.repository.CallLogRepository
import dev.badiale.callblocker.domain.repository.CallRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

@Serializable
object CallLogScreenNavigation

@Composable
fun CallLogScreen(navController: NavHostController) {
    val context = LocalContext.current
    val callLogRepository = CallLogRepository(context)
    val callLog = MutableStateFlow<List<CallRegistry>>(emptyList())
    val loading = MutableStateFlow(false)
    val hasMore by loading.collectAsState()
    val logs by callLog.collectAsState()

    val listState = rememberLazyListState()

    // Detect when user scrolls near the end

    LaunchedEffect(listState) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            return@LaunchedEffect
        }
        callLog.value = callLogRepository.findAll()
        loading.value = callLog.value.isNotEmpty()
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItem ->
                if (lastVisibleItem != null && lastVisibleItem >= logs.size - 3 && hasMore) {
                    val findAll = callLogRepository.findAll(start = lastVisibleItem)
                    loading.value = findAll.isNotEmpty()
                    callLog.value = logs + findAll
                }
            }
    }

    Column {
        LazyColumn(state = listState) {
            items(logs) { log ->
                CallRegistryComposable(
                    log = log,
                    onItemClick = { navController.navigate(CallLogDetailsNavigation(log.id)) })
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
            }

            if (hasMore) {
                item { CircularProgressIndicator() }
            }
        }
    }
}

@Composable
fun CallRegistryComposable(log: CallRegistry, onItemClick: (CallRegistry) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clickable { onItemClick(log) }
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(log.cachedPhotoUri ?: R.drawable.outline_call_24)
                    .crossfade(true)
                    .build(),
                contentDescription = "Contact photo",
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .then(Modifier),
            )
            Column {
                Text(
                    text = log.contactName ?: log.formattedNumber ?: log.number,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val text = stringResource(formatTypeText(log.type))
                    Image(
                        painter = painterResource(formatTypeDrawable(log.type)),
                        contentDescription = text,
                        modifier = Modifier.size(16.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp, 0.dp)
                    )
                    Text(
                        text = formatDate(log.date),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@StringRes
fun formatTypeText(type: Int): Int {
    return when (type) {
        INCOMING_TYPE -> R.string.call_type_incoming_type
        OUTGOING_TYPE -> R.string.call_type_outgoing_type
        MISSED_TYPE -> R.string.call_type_missed_type
        VOICEMAIL_TYPE -> R.string.call_type_voicemail_type
        REJECTED_TYPE -> R.string.call_type_rejected_type
        BLOCKED_TYPE -> R.string.call_type_blocked_type
        ANSWERED_EXTERNALLY_TYPE -> R.string.call_type_answered_externally_type
        else -> R.string.call_type_answered_unknown
    }
}

@DrawableRes
fun formatTypeDrawable(type: Int): Int {
    return when (type) {
        INCOMING_TYPE -> R.drawable.baseline_call_received_24
        OUTGOING_TYPE -> R.drawable.baseline_call_made_24
        MISSED_TYPE -> R.drawable.baseline_call_missed_24
        VOICEMAIL_TYPE -> R.drawable.baseline_voicemail_24
        REJECTED_TYPE -> R.drawable.baseline_call_end_24
        BLOCKED_TYPE -> R.drawable.baseline_block_24
        else -> R.drawable.baseline_question_mark_24
    }
}

fun formatDate(date: Date): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(Instant.ofEpochMilli(date.time))
}
