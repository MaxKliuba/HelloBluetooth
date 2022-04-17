package com.maxclub.android.hellobluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionViewModel(private val app: Application) : AndroidViewModel(app) {
    private val bluetoothService: BluetoothService = BluetoothService.get()

    val bluetoothAdapter: BluetoothAdapter
        get() = bluetoothService.bluetoothAdapter
    val bluetoothDevice: BluetoothDevice?
        get() = bluetoothService.bluetoothDevice
    val connectionState: LiveData<Int>
        get() = bluetoothService.connectionState

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()
    val isBonding: Boolean
        @SuppressLint("MissingPermission")
        get() = availableDevices.any { it.bondState == BluetoothDevice.BOND_BONDING }
    var isBluetoothEnableIntentLaunched = false

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothService.connect(app, device)
        }
    }

    fun disconnect() {
        bluetoothService.disconnect(app)
    }
}