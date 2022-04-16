package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectionViewModel : ViewModel() {
    private val bluetoothRepository: BluetoothRepository = BluetoothRepository.get()

    val bluetoothAdapter: BluetoothAdapter = bluetoothRepository.bluetoothAdapter
    var bluetoothDevice: BluetoothDevice? = bluetoothRepository.bluetoothDevice
        set(value) {
            field = value
            bluetoothRepository.bluetoothDevice = value
        }
    val connectionState: LiveData<Int>
        get() = bluetoothRepository.connectionState

    val availableDevices: MutableList<BluetoothDevice> = mutableListOf()
    var isBonding = false
}