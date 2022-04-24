package com.maxclub.android.hellobluetooth

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class HelloBluetoothApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(SettingsPreferences.getThemeMode(this))
    }
}