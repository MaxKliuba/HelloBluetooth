package com.maxclub.android.hellobluetooth.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData

interface IBluetoothConnectionCallbacks {
    fun onConnect(device: BluetoothDevice)

    fun onDisconnect()

    fun getState(): LiveData<Int>

    fun getDevice(): BluetoothDevice?
}