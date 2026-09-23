package dev.badiale.callblocker;

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog.Calls.BLOCKED_TYPE
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null;
        }

        val searchVariations = getPhoneNumberVariations(number)
        val resolver: ContentResolver = contentResolver

        for (searchNum in searchVariations) {
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
                    if (cursor.moveToNext()) {
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
                }
        }
        return null
    }

    fun getPhoneNumberVariations(number: String): List<String> {
        val searchVariations = mutableListOf<String>()

        val onlyNumbers = number.replace(Regex("\\D"), "")
        if (onlyNumbers.isNotEmpty()) {
            searchVariations.add(onlyNumbers)
        }

        try {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneUtil.parse(number, "BR")

            val nationalNumber = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                .replace(Regex("\\D"), "")

            if (nationalNumber.isNotEmpty() && !searchVariations.contains(nationalNumber)) {
                searchVariations.add(nationalNumber)
            }

            // 3. Apenas o número local (últimos 8 ou 9 dígitos), caso o contato esteja salvo sem DDD
            if (nationalNumber.length > 8) {
                // Pega os últimos 9 dígitos (geralmente celular com 9º dígito) ou 8 (fixo/antigo)
                val localNumber = nationalNumber.takeLast(9)
                if (!searchVariations.contains(localNumber)) {
                    searchVariations.add(localNumber)
                }
            }
        } catch (e: NumberParseException) {
            // Fallback caso o parsing falhe: usa os últimos 9 dígitos da string pura
            if (onlyNumbers.length > 9) {
                val fallbackLocal = onlyNumbers.takeLast(9)
                if (!searchVariations.contains(fallbackLocal)) {
                    searchVariations.add(fallbackLocal)
                }
            }
        }
        return searchVariations;
    }
}