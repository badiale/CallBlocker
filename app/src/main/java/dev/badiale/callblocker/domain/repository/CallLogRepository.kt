package dev.badiale.callblocker.domain.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

data class CallRegistry(
    val id: Int,
    val formattedNumber: String?,
    val number: String,
    val contactName: String?,
    val type: Int,
    val date: Date,
    val cachedPhotoUri: Uri?,
    val viaNumber: String?,
)

class CallLogRepository(private val context: Context) {

    suspend fun findAll(start: Int = 0, maxResults: Int = 100): List<CallRegistry> {
        return load(start, maxResults, null, null).map {
            CallRegistry(
                id = it[CallLog.Calls._ID]!!.toInt(),
                formattedNumber = it[CallLog.Calls.CACHED_FORMATTED_NUMBER],
                number = it[CallLog.Calls.NUMBER]!!,
                contactName = it[CallLog.Calls.CACHED_NAME],
                type = it[CallLog.Calls.TYPE]!!.toInt(),
                date = Date(it[CallLog.Calls.DATE]?.toLong() ?: 0),
                cachedPhotoUri = it[CallLog.Calls.CACHED_PHOTO_URI]?.ifBlank { null }?.toUri(),
                viaNumber = it["phone_account_address"],
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
            cursor.move(start)
            while (cursor.moveToNext() && count++ < maxResults) {
                val all = HashMap<String, String>();
                for (i in 0..cursor.columnCount - 1) {
                    cursor.getString(i)?.let {
                        if (!it.isEmpty()) {
                            all[cursor.columnNames[i]] = it
                        }
                    }
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