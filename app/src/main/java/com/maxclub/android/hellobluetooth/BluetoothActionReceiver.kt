package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val LOG_TAG = "BluetoothActionReceiver"

class BluetoothActionReceiver : BroadcastReceiver() {
    private val bluetoothRepository: BluetoothRepository = BluetoothRepository.get()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                Log.i(LOG_TAG, "State -> $state")
                bluetoothRepository.state.value = state
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val connectionState = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_CONNECTION_STATE,
                    BluetoothAdapter.ERROR
                )
                Log.i(LOG_TAG, "Connection state -> $connectionState")
                // bluetoothRepository.state.value = connectionState
            }
            else -> {
                Log.i(LOG_TAG, "Other action")
            }
        }
    }
}