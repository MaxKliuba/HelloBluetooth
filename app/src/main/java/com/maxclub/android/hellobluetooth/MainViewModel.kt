package com.maxclub.android.hellobluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.NavDestination

class MainViewModel(context: Application) : AndroidViewModel(context) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    init {
        val state = if (bluetoothAdapter.isEnabled) {
            if (BluetoothService.isConnected) {
                BluetoothAdapter.STATE_CONNECTED
            } else {
                BluetoothAdapter.STATE_DISCONNECTED
            }
        } else {
            bluetoothAdapter.state
        }
        BluetoothService.updateState(state)
    }

    lateinit var currentDestination: NavDestination
}