package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.lifecycle.AndroidViewModel

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val bluetoothManager: BluetoothManager =
        application.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()
}