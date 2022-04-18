package com.maxclub.android.hellobluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionViewModel(private val context: Application) : AndroidViewModel(context) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            BluetoothService.connect(context, device)
        }
    }

    fun disconnect() {
        BluetoothService.disconnect(context)
    }
}