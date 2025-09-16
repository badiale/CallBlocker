package dev.badiale.callblocker.domain.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.core.database.getStringOrNull
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

data class CallRegistry(
    val id: Int?,
    val formattedNumber: String?,
    val number: String,
    val contactName: String?,
    val type: Int,
    val date: Date,
    val cachedPhotoUri: Uri?,
)

class CallLogRepository(context: Context) {
    private companion object {
        const val TABLE_NAME = "CALL_LOG"
        const val COLUMN_ID = "id"
        const val COLUMN_FORMATTED_NUMBER = "formattedNumber"
        const val COLUMN_NUMBER = "number"
        const val COLUMN_CONTACT_NAME = "contactName"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DATE = "date"
        const val COLUMN_CACHED_PHOTO_URI = "cachedPhotoUri"
    }

    val dbService = DatabaseService(context)

    suspend fun insert(call: CallRegistry) = withContext(Dispatchers.IO) {
        dbService.insertOrThrow(
            TABLE_NAME,
            null,
            ContentValues().apply {
                put(COLUMN_FORMATTED_NUMBER, call.formattedNumber)
                put(COLUMN_NUMBER, call.number)
                put(COLUMN_CONTACT_NAME, call.contactName)
                put(COLUMN_TYPE, call.type)
                put(COLUMN_DATE, call.date.time)
                put(COLUMN_CACHED_PHOTO_URI, call.cachedPhotoUri?.toString())
            }
        );
    }

    suspend fun findAll(start: Int = 0, maxResults: Int = 100): List<CallRegistry> {
        return load(start, maxResults, null, null)
    }

    private suspend fun load(
        start: Int,
        maxResults: Int,
        selection: String?,
        selectionArgs: Array<String?>?
    ): List<CallRegistry> = withContext(Dispatchers.IO) {
        var count = 0
        val callLogList = mutableListOf<CallRegistry>()

        val cursor = dbService.query(
            TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_DATE DESC",
            null
        )


        cursor?.use { cursor ->
            cursor.move(start)

            val columnIdxId = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val columnIdxFormattedNumber = cursor.getColumnIndexOrThrow(COLUMN_FORMATTED_NUMBER)
            val columnIdxNumber = cursor.getColumnIndexOrThrow(COLUMN_NUMBER)
            val columnIdxContactName = cursor.getColumnIndexOrThrow(COLUMN_CONTACT_NAME)
            val columnIdxType = cursor.getColumnIndexOrThrow(COLUMN_TYPE)
            val columnIdxDate = cursor.getColumnIndexOrThrow(COLUMN_DATE)
            val columnIdxCachedPhotoUri = cursor.getColumnIndexOrThrow(COLUMN_CACHED_PHOTO_URI)

            while (cursor.moveToNext() && count++ < maxResults) {
                callLogList += CallRegistry(
                    id = cursor.getString(columnIdxId)!!.toInt(),
                    formattedNumber = cursor.getStringOrNull(columnIdxFormattedNumber),
                    number = cursor.getString(columnIdxNumber)!!,
                    contactName = cursor.getStringOrNull(columnIdxContactName),
                    type = cursor.getInt(columnIdxType),
                    date = Date(cursor.getLong(columnIdxDate)),
                    cachedPhotoUri = cursor.getStringOrNull(columnIdxCachedPhotoUri)?.toUri(),
                )
            }
        }

        return@withContext callLogList
    }

    suspend fun findById(logId: Int) =
        load(
            0,
            1,
            "$COLUMN_ID = ?",
            arrayOf(logId.toString())
        ).firstOrNull()
}