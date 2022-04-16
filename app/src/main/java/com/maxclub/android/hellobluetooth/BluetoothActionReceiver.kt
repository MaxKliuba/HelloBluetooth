package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
                Log.i(LOG_TAG, "BluetoothActionReceiver -> ACTION_STATE_CHANGED: $state")
                Log.i(
                    LOG_TAG,
                    "BluetoothActionReceiver -> State changed: $state"
                )
                bluetoothRepository.connectionState.value = state
            }
            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val connectionState = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_CONNECTION_STATE,
                    BluetoothAdapter.ERROR
                )
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_CONNECTION_STATE_CHANGED: ${it.address} ($connectionState)"
                    )
                    bluetoothRepository.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            Log.i(
                                LOG_TAG,
                                "BluetoothActionReceiver -> State changed: $connectionState"
                            )
                            bluetoothRepository.connectionState.value = connectionState

                            if (connectionState == BluetoothAdapter.STATE_DISCONNECTED ||
                                connectionState == BluetoothAdapter.ERROR
                            ) {
                                bluetoothRepository.bluetoothDevice = null
                            }
                        }
                    }
                }
            }
            else -> {
                Log.i(LOG_TAG, "BluetoothActionReceiver -> ACTION_UNKNOWN")
            }
        }
    }
}