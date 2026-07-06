package com.reproflex3.rplink.data.preferences

interface PreferencesManager {

    fun getInt(key: String): Int

    fun putInt(key: String, value: Int)

    fun getString(key: String): String

    fun putString(key: String, value: String)
}