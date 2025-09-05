package dev.badiale.callblocker.services

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class PreferenceService(context: Context) {
    private companion object {
        const val PREFERENCE_NAME = "callBlockerGeneral"
        const val BLOCK_UNKNOWN_NUMBER = "BLOCK_UNKNOWN_NUMBER"
    }

    val preference = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE)

    fun isBlockUnknownNumber(): Boolean {
        return preference.getBoolean(BLOCK_UNKNOWN_NUMBER, true)
    }

    fun setBlockUnknownNumber(block: Boolean) {
        preference.edit(commit = true) {
            putBoolean(BLOCK_UNKNOWN_NUMBER, block)
        };
    }
}