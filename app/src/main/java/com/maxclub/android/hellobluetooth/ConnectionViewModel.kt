package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    private val bluetoothService: BluetoothService = BluetoothService.get()

    val bluetoothAdapter: BluetoothAdapter
        get() = bluetoothService.bluetoothAdapter
    val bluetoothDevice: BluetoothDevice?
        get() = bluetoothService.bluetoothDevice
    val connectionState: LiveData<Int>
        get() = bluetoothService.connectionState

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()
    var isBonding = false

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothService.connect(device)
        }
    }

    fun cancel() {
        bluetoothService.cancel()
    }
}