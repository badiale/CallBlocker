package dev.badiale.callblocker.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog.Calls
import android.provider.CallLog.Calls.ANSWERED_EXTERNALLY_TYPE
import android.provider.CallLog.Calls.BLOCKED_TYPE
import android.provider.CallLog.Calls.INCOMING_TYPE
import android.provider.CallLog.Calls.MISSED_TYPE
import android.provider.CallLog.Calls.OUTGOING_TYPE
import android.provider.CallLog.Calls.REJECTED_TYPE
import android.provider.CallLog.Calls.VOICEMAIL_TYPE
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.badiale.callblocker.R
import dev.badiale.callblocker.domain.repository.CallLogRepository
import dev.badiale.callblocker.domain.repository.CallRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

@Preview
@Composable
fun CallLogScreen() {
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

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        LazyColumn(state = listState) {
            items(logs) { log ->
                Text(
                    text = log.formattedNumber ?: log.number,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(formatType(log.type)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatDate(log.date),
                    style = MaterialTheme.typography.bodySmall
                )
                log.callScreeningAppName?.let {
                    Text(
                        text = "callScreeningAppName: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.countryIso?.let {
                    Text(
                        text = "countryIso: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.cachedPhotoId?.let {
                    Text(
                        text = "cachedPhotoId: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.cachedPhotoUri?.let {
                    Text(
                        text = "cachedPhotoUri: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.missedReason?.let {
                    Text(
                        text = "missedReason: ${formatMissedReason(it)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.blockReason?.let {
                    Text(
                        text = "blockReason: ${formatBlockedReason(it)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "new: ${log.new}",
                    style = MaterialTheme.typography.bodySmall
                )
                log.viaNumber?.let {
                    Text(
                        text = "viaNumber: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.location?.let {
                    Text(
                        text = "location: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                log.geocodedLocation?.let {
                    Text(
                        text = "geocodedLocation: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
            }

            if (hasMore) {
                item { CircularProgressIndicator() }
            }
        }
    }
}

@StringRes
fun formatType(type: Int): Int {
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

fun formatMissedReason(type: Long): String {
    return when (type) {
        Calls.MISSED_REASON_NOT_MISSED -> "MISSED_REASON_NOT_MISSED"
        Calls.AUTO_MISSED_EMERGENCY_CALL -> "AUTO_MISSED_EMERGENCY_CALL"
        Calls.AUTO_MISSED_MAXIMUM_RINGING -> "AUTO_MISSED_MAXIMUM_RINGING"
        Calls.AUTO_MISSED_MAXIMUM_DIALING -> "AUTO_MISSED_MAXIMUM_DIALING"
        Calls.USER_MISSED_NO_ANSWER -> "USER_MISSED_NO_ANSWER"
        Calls.USER_MISSED_SHORT_RING -> "USER_MISSED_SHORT_RING"
        Calls.USER_MISSED_DND_MODE -> "USER_MISSED_DND_MODE"
        Calls.USER_MISSED_LOW_RING_VOLUME -> "USER_MISSED_LOW_RING_VOLUME"
        Calls.USER_MISSED_NO_VIBRATE -> "USER_MISSED_NO_VIBRATE"
        Calls.USER_MISSED_CALL_SCREENING_SERVICE_SILENCED -> "USER_MISSED_CALL_SCREENING_SERVICE_SILENCED"
        Calls.USER_MISSED_CALL_FILTERS_TIMEOUT -> "USER_MISSED_CALL_FILTERS_TIMEOUT"
        else -> "unknown"
    }
}

fun formatBlockedReason(type: Int): String {
    return when (type) {
        Calls.BLOCK_REASON_NOT_BLOCKED -> "BLOCK_REASON_NOT_BLOCKED"
        Calls.BLOCK_REASON_CALL_SCREENING_SERVICE -> "BLOCK_REASON_CALL_SCREENING_SERVICE"
        Calls.BLOCK_REASON_DIRECT_TO_VOICEMAIL -> "BLOCK_REASON_DIRECT_TO_VOICEMAIL"
        Calls.BLOCK_REASON_BLOCKED_NUMBER -> "BLOCK_REASON_BLOCKED_NUMBER"
        Calls.BLOCK_REASON_UNKNOWN_NUMBER -> "BLOCK_REASON_UNKNOWN_NUMBER"
        Calls.BLOCK_REASON_RESTRICTED_NUMBER -> "BLOCK_REASON_RESTRICTED_NUMBER"
        Calls.BLOCK_REASON_PAY_PHONE -> "BLOCK_REASON_PAY_PHONE"
        Calls.BLOCK_REASON_NOT_IN_CONTACTS -> "BLOCK_REASON_NOT_IN_CONTACTS"
        else -> "unknown"
    }
}

fun formatDate(date: Date): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(Instant.ofEpochMilli(date.time))
}
