package dev.badiale.callblocker;

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.CallLog.Calls.BLOCKED_TYPE
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import dev.badiale.callblocker.domain.repository.CallLogRepository
import dev.badiale.callblocker.domain.repository.CallRegistry
import dev.badiale.callblocker.services.PreferenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Date

data class Contact(val name: String, val photoUri: Uri?)

class CallBlockerScreeningService : CallScreeningService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle.schemeSpecificPart
        Log.d("CallBlocker", "Incoming call: $number")
        val preferenceService = PreferenceService(this)

        val callResponseBuilder = CallResponse.Builder()
        var type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            && callDetails.callDirection != Call.Details.DIRECTION_INCOMING
        ) android.provider.CallLog.Calls.OUTGOING_TYPE else android.provider.CallLog.Calls.INCOMING_TYPE
        val contact = findContactOrNull(number)
        if (preferenceService.isBlockUnknownNumber() && contact == null) {
            callResponseBuilder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
            type = BLOCKED_TYPE
        }

        respondToCall(callDetails, callResponseBuilder.build())
        val callLogRepository = CallLogRepository(this)
        scope.launch {
            callLogRepository.insert(
                CallRegistry(
                    id = null,
                    isContact = contact != null,
                    formattedNumber = callDetails.callerDisplayName,
                    number = number,
                    contactName = contact?.name,
                    type = type,
                    date = Date(callDetails.creationTimeMillis),
                    cachedPhotoUri = contact?.photoUri,
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun findContactOrNull(number: String): Contact? {
        val resolver: ContentResolver = contentResolver
        val uri: Uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(number)
            .build()
        resolver.query(
            uri,
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            ),
            null,
            null,
            null
        )
            ?.use { cursor ->
                if (!cursor.moveToNext()) {
                    return null
                }
                val columnIndexDisplayName =
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                val columnIndexPhotoUri =
                    cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                return Contact(
                    name = cursor.getStringOrNull(columnIndexDisplayName)
                        ?: number,
                    photoUri = cursor.getStringOrNull(columnIndexPhotoUri)?.toUri()
                )
            }
        return null
    }
}