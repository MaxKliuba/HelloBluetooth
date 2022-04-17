package com.maxclub.android.hellobluetooth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination

class MainViewModel : ViewModel() {
    private val bluetoothService: BluetoothService = BluetoothService.get()

    val connectionState: LiveData<Int>
        get() = bluetoothService.connectionState

    fun refreshConnectionState() {
        bluetoothService.refreshConnectionState()
    }
    lateinit var currentDestination: NavDestination
}