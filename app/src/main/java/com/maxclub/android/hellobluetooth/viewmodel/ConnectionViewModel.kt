package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.lifecycle.AndroidViewModel

class ConnectionViewModel(context: Application) : AndroidViewModel(context) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()
}