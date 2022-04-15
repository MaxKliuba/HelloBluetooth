package com.maxclub.android.hellobluetooth

import android.app.Application

class HelloBluetoothApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BluetoothRepository.initialize(this)
    }
}