package com.android.zebraassistrtmp.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var baseURL: String?
        get() = sharedPreferences.getString(KEY_BASEURL, "")
        set(baseURL) {
            sharedPreferences.edit().putString(KEY_BASEURL, baseURL).apply()
        }

    // Clear all data in SharedPreferences
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "ZAPreferences"
        private const val KEY_BASEURL = "baseUrl"
    }
}