package dev.badiale.callblocker.domain.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.Date

data class CallRegistry(
    val id: Int,
    val formattedNumber: String?,
    val number: String,
    val type: Int,
    val date: Date,
    val duration: Duration,
    val callScreeningAppName: String?,
    val countryIso: String?,
    val cachedPhotoId: Long?,
    val cachedPhotoUri: Uri?,

    /**
     * USER_MISSED_SHORT_RING
     * USER_MISSED_DND_MODE
     * USER_MISSED_LOW_RING_VOLUME
     * USER_MISSED_NO_VIBRATE
     * USER_MISSED_CALL_SCREENING_SERVICE_SILENCED
     * USER_MISSED_CALL_FILTERS_TIMEOUT
     * */
    val missedReason: Long?,

    /**
     * BLOCK_REASON_NOT_BLOCKED
     * BLOCK_REASON_CALL_SCREENING_SERVICE
     * BLOCK_REASON_DIRECT_TO_VOICEMAIL
     * BLOCK_REASON_BLOCKED_NUMBER
     * BLOCK_REASON_UNKNOWN_NUMBER
     * BLOCK_REASON_RESTRICTED_NUMBER
     * BLOCK_REASON_PAY_PHONE
     * BLOCK_REASON_NOT_IN_CONTACTS
     * */
    val blockReason: Int?,
    val new: Boolean,
    val viaNumber: String?,
    val location: String?,
    val geocodedLocation: String?,
)

class CallLogRepository(private val context: Context) {

    suspend fun findAll(start: Int = 0, maxResults: Int = 10): List<CallRegistry> {
        return load(start, maxResults, null, null).map {
            val callScreeningAppNameIdx =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it[CallLog.Calls.CALL_SCREENING_APP_NAME]
                } else {
                    null
                }
            val missedReasonIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it[CallLog.Calls.MISSED_REASON]
            } else {
                null
            }
            val blockReasonIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it[CallLog.Calls.BLOCK_REASON]
            } else {
                null
            }
            val locationIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it[CallLog.Calls.LOCATION]
            } else {
                null
            }

            CallRegistry(
                id = it[CallLog.Calls._ID]!!.toInt(),
                formattedNumber = it[CallLog.Calls.CACHED_FORMATTED_NUMBER],
                number = it[CallLog.Calls.NUMBER]!!,
                type = it[CallLog.Calls.TYPE]!!.toInt(),
                date = Date(it[CallLog.Calls.DATE]?.toLong() ?: 0),
                duration = Duration.ofSeconds(it[CallLog.Calls.DURATION]?.toLong() ?: 0),
                callScreeningAppName = callScreeningAppNameIdx,
                countryIso = it[CallLog.Calls.COUNTRY_ISO],
                cachedPhotoId = it[CallLog.Calls.CACHED_PHOTO_ID]?.toLong(),
                cachedPhotoUri = it[CallLog.Calls.CACHED_PHOTO_URI]?.ifBlank { null }?.toUri(),
                missedReason = missedReasonIdx?.toLong(),
                blockReason = blockReasonIdx?.toInt(),
                new = it[CallLog.Calls.NEW]?.toInt() == 1,
                viaNumber = it[CallLog.Calls.VIA_NUMBER],
                location = locationIdx,
                geocodedLocation = it[CallLog.Calls.GEOCODED_LOCATION]
            )
        }
    }

    private suspend fun load(
        start: Int,
        maxResults: Int,
        selection: String?,
        selectionArgs: Array<String>?
    ): List<Map<String, String>> = withContext(Dispatchers.IO) {
        var count = 0
        val callLogList = mutableListOf<Map<String, String>>()

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, selection, selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )


        cursor?.use { cursor ->
            val all = HashMap<String, String>();
            cursor.move(start)
            while (cursor.moveToNext() && count++ < maxResults) {
                for (i in 0..cursor.columnCount - 1) {
                    cursor.getString(i)?.let { all[cursor.columnNames[i]] = it }
                }
                callLogList += all
            }
        }

        return@withContext callLogList
    }

    suspend fun findById(logId: Int) =
        load(
            0,
            1,
            "_id = ?",
            arrayOf(logId.toString())
        ).firstOrNull() ?: emptyMap()
}