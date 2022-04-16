package com.maxclub.android.hellobluetooth

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class HelloBluetoothApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BluetoothRepository.initialize(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

}