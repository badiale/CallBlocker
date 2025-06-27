package dev.badiale.callblocker;

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallBlockerScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle.schemeSpecificPart
        Log.d("CallBlocker", "Incoming call: $number")

        val callResponseBuilder = CallResponse.Builder()
        if (!isInContacts(number)) {
            callResponseBuilder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
        }
        respondToCall(callDetails, callResponseBuilder.build())
    }

    private fun isInContacts(number: String): Boolean {
        val resolver: ContentResolver = contentResolver
        val uri: Uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(number)
            .build()
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            return cursor.count > 0
        }
        return false
    }
}