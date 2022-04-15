package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import java.lang.IllegalStateException

class BluetoothRepository private constructor(context: Context) {
    val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    var bluetoothDevice: BluetoothDevice? = null

    val state = MutableLiveData(bluetoothAdapter.state)

    companion object {
        private var instance: BluetoothRepository? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = BluetoothRepository(context)
            }
        }

        fun get(): BluetoothRepository =
            instance ?: throw IllegalStateException("BluetoothRepository must be initialized")
    }
}