package com.reproflex3.rplink.data.preferences

import android.content.Context
import javax.inject.Inject

class PreferencesManagerImpl @Inject constructor(private val context: Context) :
    PreferencesManager {

    private fun getSharedPreferences() =
        context.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE)

    private val sharedPrefs by lazy { getSharedPreferences() }

    override fun getInt(key: String): Int = sharedPrefs.getInt(key, 0)

    override fun putInt(key: String, value: Int) {
        with(sharedPrefs.edit()) {
            putInt(key, value)
            apply()
        }
    }

    override fun getString(key: String): String = sharedPrefs.getString(key, "").toString()

    override fun putString(key: String, value: String) {
        with(sharedPrefs.edit()) {
            putString(key, value)
            apply()
        }
    }

    companion object {
        private const val APP_DATA = "app_data"

        const val TOKEN = "TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
    }
}