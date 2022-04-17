package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

private const val LOG_TAG = "BluetoothStateBroadcastReceiver"

class BluetoothStateBroadcastReceiver : BroadcastReceiver() {
    interface BluetoothStateListener {
        fun onStateChanged(state: Int)

        fun onConnectionStateChanged(state: Int, device: BluetoothDevice)
    }

    private var listener: BluetoothStateListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                Log.i(LOG_TAG, "ACTION_STATE_CHANGED -> $state")
                listener?.onStateChanged(state)
            }
            ACTION_DEVICE_DISCONNECTED -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_DEVICE_DISCONNECTED -> ${it.address}")
                    listener?.onConnectionStateChanged(
                        BluetoothAdapter.STATE_DISCONNECTED,
                        it
                    )
                }
            }
            ACTION_DEVICE_CONNECTED -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_DEVICE_CONNECTED -> ${it.address}")
                    listener?.onConnectionStateChanged(
                        BluetoothAdapter.STATE_CONNECTED,
                        it
                    )
                }
            }
            ACTION_DEVICE_DISCONNECTING -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_DEVICE_DISCONNECTING -> ${it.address}")
                    listener?.onConnectionStateChanged(
                        BluetoothAdapter.STATE_DISCONNECTING,
                        it
                    )
                }
            }
            ACTION_DEVICE_CONNECTING -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_DEVICE_CONNECTING -> ${it.address}")
                    listener?.onConnectionStateChanged(
                        BluetoothAdapter.STATE_CONNECTING,
                        it
                    )
                }
            }
            ACTION_CONNECTION_ERROR -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_CONNECTION_ERROR -> ${it.address}")
                    listener?.onConnectionStateChanged(BluetoothAdapter.STATE_DISCONNECTED, it)
                }
            }
            else -> {
                Log.i(LOG_TAG, "UNKNOWN_ACTION -> ${intent.action}")
            }
        }
    }

    fun register(context: Context, listener: BluetoothStateListener) {
        val filter = IntentFilter().apply {
            addAction(ACTION_STATE_CHANGED)
            addAction(ACTION_DEVICE_DISCONNECTED)
            addAction(ACTION_DEVICE_CONNECTED)
            addAction(ACTION_DEVICE_DISCONNECTING)
            addAction(ACTION_DEVICE_CONNECTING)
            addAction(ACTION_CONNECTION_ERROR)
        }
        context.registerReceiver(this, filter)
        this.listener = listener
    }

    fun unregister(context: Context) {
        listener = null
        context.unregisterReceiver(this)
    }

    companion object {
        const val ACTION_STATE_CHANGED = BluetoothAdapter.ACTION_STATE_CHANGED
        const val ACTION_DEVICE_DISCONNECTED = BluetoothDevice.ACTION_ACL_DISCONNECTED
        const val ACTION_DEVICE_CONNECTED = BluetoothDevice.ACTION_ACL_CONNECTED
        const val ACTION_DEVICE_DISCONNECTING =
            "com.maxclub.android.hellobluetooth.action.DEVICE_DISCONNECTING"
        const val ACTION_DEVICE_CONNECTING =
            "com.maxclub.android.hellobluetooth.action.DEVICE_CONNECTING"
        const val ACTION_CONNECTION_ERROR =
            "com.maxclub.android.hellobluetooth.action.CONNECTION_ERROR"

        const val EXTRA_STATE = BluetoothAdapter.EXTRA_STATE
        const val EXTRA_DEVICE = BluetoothDevice.EXTRA_DEVICE
    }
}