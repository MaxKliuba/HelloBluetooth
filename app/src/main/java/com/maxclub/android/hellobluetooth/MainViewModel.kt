package com.maxclub.android.hellobluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import kotlinx.coroutines.launch

class MainViewModel(context: Application) : AndroidViewModel(context) {
    val bluetoothService: BluetoothService = BluetoothService(context)

    init {
        val state = if (bluetoothService.adapter.isEnabled) {
            if (bluetoothService.isSocketConnected) {
                BluetoothAdapter.STATE_CONNECTED
            } else {
                BluetoothAdapter.STATE_DISCONNECTED
            }
        } else {
            bluetoothService.adapter.state
        }
        bluetoothService.updateState(state)
    }

    lateinit var currentDestination: NavDestination

    fun connect(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothService.connect(device)
        }
    }

    fun disconnect() {
        bluetoothService.disconnect()
    }

    fun startListening() {
        viewModelScope.launch {
            bluetoothService.startListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}