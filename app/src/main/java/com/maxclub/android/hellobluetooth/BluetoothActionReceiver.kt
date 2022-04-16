package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val LOG_TAG = "BluetoothActionReceiver"

class BluetoothActionReceiver : BroadcastReceiver() {
    private val bluetoothService: BluetoothService = BluetoothService.get()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                Log.i(LOG_TAG, "BluetoothActionReceiver -> ACTION_STATE_CHANGED: $state")
                Log.i(
                    LOG_TAG,
                    "BluetoothActionReceiver -> State changed: $state"
                )
                bluetoothService.connectionState.value = state
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_ACL_CONNECTED: ${it.address}"
                    )
                    bluetoothService.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            bluetoothService.connectionState.value =
                                BluetoothAdapter.STATE_CONNECTED
                        }
                    }
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_ACL_DISCONNECT_REQUESTED: ${it.address}"
                    )
                    bluetoothService.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            bluetoothService.connectionState.value =
                                BluetoothAdapter.STATE_DISCONNECTING
                        }
                    }
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED, BluetoothService.ACTION_CONNECTION_ERROR -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_ACL_DISCONNECTED: ${it.address}"
                    )
                    bluetoothService.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            bluetoothService.connectionState.value =
                                BluetoothAdapter.STATE_DISCONNECTED
                            bluetoothService.bluetoothDevice = null
                            bluetoothService.cancel()
                        }
                    }
                }
            }
            BluetoothService.ACTION_ACL_CONNECTING -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_ACL_CONNECTING: ${it.address}"
                    )
                    bluetoothService.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            bluetoothService.connectionState.value =
                                BluetoothAdapter.STATE_CONNECTING
                        }
                    }
                }
            }
            BluetoothService.ACTION_ACL_DISCONNECTING -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    Log.i(
                        LOG_TAG,
                        "BluetoothActionReceiver -> ACTION_ACL_DISCONNECTING: ${it.address}"
                    )
                    bluetoothService.bluetoothDevice?.let { bluetoothDevice ->
                        if (bluetoothDevice == device) {
                            bluetoothService.connectionState.value =
                                BluetoothAdapter.STATE_DISCONNECTING
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