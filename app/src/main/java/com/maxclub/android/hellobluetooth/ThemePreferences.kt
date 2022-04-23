package com.maxclub.android.hellobluetooth

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val PREF_MODE = "prefThemeMode"

object ThemePreferences {
    fun getMode(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(PREF_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun setMode(context: Context, mode: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putInt(PREF_MODE, mode)
            }
    }
}