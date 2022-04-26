package com.maxclub.android.hellobluetooth.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val PREF_THEME_MODE = "prefThemeMode"

object SettingsPreferences {
    fun getThemeMode(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun setThemeMode(context: Context, mode: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putInt(PREF_THEME_MODE, mode)
            }
    }
}