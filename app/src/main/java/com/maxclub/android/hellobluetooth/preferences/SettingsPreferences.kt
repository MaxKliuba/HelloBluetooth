package com.maxclub.android.hellobluetooth.preferences

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.maxclub.android.hellobluetooth.model.Language

private const val PREF_THEME_MODE = "prefThemeMode"

private const val PREF_LANGUAGE = "prefLanguage"

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

    fun getLanguage(context: Context): Language =
        Language.values().getOrNull(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_LANGUAGE, Language.DEFAULT.ordinal)
        ) ?: Language.DEFAULT

    fun setLanguage(context: Context, language: Language) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putInt(PREF_LANGUAGE, language.ordinal)
            }
    }
}