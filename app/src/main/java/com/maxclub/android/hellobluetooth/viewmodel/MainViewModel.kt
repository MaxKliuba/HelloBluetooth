package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.BluetoothService
import com.maxclub.android.hellobluetooth.data.Command
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bluetoothService: BluetoothService = BluetoothService(application)
    val commands: MutableLiveData<List<Command>> = MutableLiveData(emptyList())

    init {
        bluetoothService.updateState(
            if (bluetoothService.adapter.isEnabled) {
                if (bluetoothService.isSocketConnected) {
                    BluetoothAdapter.STATE_CONNECTED
                } else {
                    BluetoothAdapter.STATE_DISCONNECTED
                }
            } else {
                bluetoothService.adapter.state
            }
        )
    }

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
}