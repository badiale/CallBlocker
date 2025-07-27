package dev.badiale.callblocker.domain.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.Date
import androidx.core.net.toUri

data class CallRegistry(
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

    suspend fun findAll(start: Int = 0, maxResults: Int = 10): List<CallRegistry> =
        withContext(Dispatchers.IO) {
            var count = 0
            val callLogList = mutableListOf<CallRegistry>()

            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                it.move(start)

                val formattedNumberIdx = it.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER)
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)
                val callScreeningAppNameIdx =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        it.getColumnIndex(CallLog.Calls.CALL_SCREENING_APP_NAME)
                    } else {
                        -1
                    }
                val countryIsoIdx = it.getColumnIndex(CallLog.Calls.COUNTRY_ISO)
                val cachedPhotoIdIdx = it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_ID)
                val cachedPhotoUriIdx = it.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI)
                val missedReasonIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    it.getColumnIndex(CallLog.Calls.MISSED_REASON)
                } else {
                    -1
                }
                val blockReasonIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.getColumnIndex(CallLog.Calls.BLOCK_REASON)
                } else {
                    -1
                }
                val newIdx = it.getColumnIndex(CallLog.Calls.NEW)
                val viaNumberIdx = it.getColumnIndex(CallLog.Calls.VIA_NUMBER)
                val locationIdx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    it.getColumnIndex(CallLog.Calls.LOCATION)
                } else {
                    -1
                }
                val geocodedLocationIdx = it.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION)

                while (it.moveToNext() && count++ < maxResults) {
                    callLogList += CallRegistry(
                        formattedNumber = it.getString(formattedNumberIdx),
                        number = it.getString(numberIdx),
                        type = it.getInt(typeIdx),
                        date = Date(it.getLong(dateIdx)),
                        duration = Duration.ofSeconds(it.getLong(durationIdx)),
                        callScreeningAppName = it.getString(callScreeningAppNameIdx),
                        countryIso = it.getString(countryIsoIdx),
                        cachedPhotoId = it.getLongOrNull(cachedPhotoIdIdx),
                        cachedPhotoUri = it.getString(cachedPhotoUriIdx)?.toUri(),
                        missedReason = it.getLongOrNull(missedReasonIdx),
                        blockReason = it.getIntOrNull(blockReasonIdx),
                        new = it.getIntOrNull(newIdx) == 1,
                        viaNumber = it.getString(viaNumberIdx),
                        location = it.getString(locationIdx),
                        geocodedLocation = it.getString(geocodedLocationIdx),
                    )
                }
            }

            return@withContext callLogList
        }
}