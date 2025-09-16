package dev.badiale.callblocker.domain.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction

class DatabaseService(context: Context) {
    private companion object {
        val MIGRATIONS = listOf<(db: SQLiteDatabase) -> Unit>(
            { db ->
                db.execSQL(
                    """CREATE TABLE CALL_LOG (
                        id integer primary key AUTOINCREMENT,
                        formattedNumber varchar(50),
                        number varchar(50) not null,
                        contactName varchar(50),
                        type number not null,
                        date number not null,
                        cachedPhotoUri text)
                    """
                )
            }

        )
        val CURRENT_VERSION = MIGRATIONS.size;
    }

    val db: SQLiteDatabase = context.openOrCreateDatabase("database.db", Context.MODE_PRIVATE, null)

    init {
        while (db.version < CURRENT_VERSION) {
            db.transaction {
                MIGRATIONS[db.version](db)
            }
            db.version++
        }
    }

    fun insertOrThrow(
        table: String,
        nullColumnHack: String?,
        values: ContentValues?
    ) = db.transaction { db.insertOrThrow(table, nullColumnHack, values) }

    fun query(
        table: String,
        columns: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?
    ): Cursor? = db.query(
        table,
        columns,
        selection,
        selectionArgs,
        groupBy,
        having,
        orderBy,
        limit
    )
}