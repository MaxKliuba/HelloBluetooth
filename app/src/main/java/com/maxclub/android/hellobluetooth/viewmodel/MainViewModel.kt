package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.bluetooth.BluetoothService
import com.maxclub.android.hellobluetooth.model.Command
import com.maxclub.android.hellobluetooth.repository.CommandRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val bluetoothService: BluetoothService = BluetoothService(application)

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

    fun startListening() {
        viewModelScope.launch {
            bluetoothService.startListening()
        }
    }

    fun disconnect() {
        bluetoothService.disconnect()
    }

    fun getCommand(): LiveData<Command> = CommandRepository.command

    fun addCommand(command: Command) {
        CommandRepository.addCommand(command)
    }

    override fun onCleared() {
        super.onCleared()
        CommandRepository.clearCommands()
        bluetoothService.stopListening()
    }
}