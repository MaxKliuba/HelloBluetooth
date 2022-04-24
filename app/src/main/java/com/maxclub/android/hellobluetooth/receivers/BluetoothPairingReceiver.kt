package com.maxclub.android.hellobluetooth.receivers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

private const val LOG_TAG = "BluetoothPairingReceiver"

class BluetoothPairingReceiver : BroadcastReceiver() {
    interface Callbacks {
        fun onDiscoveryStarted()

        fun onDiscoveryFinished()

        fun onDeviceFound(device: BluetoothDevice)

        fun onBoundStateChanged(state: Int, device: BluetoothDevice)
    }

    private var callbacks: Callbacks? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISCOVERY_STARTED -> {
                Log.i(LOG_TAG, "ACTION_DISCOVERY_STARTED")
                callbacks?.onDiscoveryStarted()
            }
            ACTION_DISCOVERY_FINISHED -> {
                Log.i(LOG_TAG, "ACTION_DISCOVERY_FINISHED")
                callbacks?.onDiscoveryFinished()
            }
            ACTION_FOUND -> {
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_FOUND -> ${it.address}")
                    callbacks?.onDeviceFound(it)
                }
            }
            ACTION_BOND_STATE_CHANGED -> {
                val state = intent.getIntExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                val device: BluetoothDevice? = intent.getParcelableExtra(EXTRA_DEVICE)
                device?.let {
                    Log.i(LOG_TAG, "ACTION_BOND_STATE_CHANGED -> ${it.address} ($state)")
                    callbacks?.onBoundStateChanged(state, it)
                }
            }
            else -> {
                Log.i(LOG_TAG, "UNKNOWN_ACTION -> ${intent.action}")
            }
        }
    }

    fun register(context: Context, listener: Callbacks) {
        val filter = IntentFilter().apply {
            addAction(ACTION_DISCOVERY_STARTED)
            addAction(ACTION_DISCOVERY_FINISHED)
            addAction(ACTION_FOUND)
            addAction(ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(this, filter)
        callbacks = listener
    }

    fun unregister(context: Context) {
        callbacks = null
        context.unregisterReceiver(this)
    }

    companion object {
        const val ACTION_DISCOVERY_STARTED = BluetoothAdapter.ACTION_DISCOVERY_STARTED
        const val ACTION_DISCOVERY_FINISHED = BluetoothAdapter.ACTION_DISCOVERY_FINISHED
        const val ACTION_FOUND = BluetoothDevice.ACTION_FOUND
        const val ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED

        const val EXTRA_DEVICE = BluetoothDevice.EXTRA_DEVICE
        const val EXTRA_BOND_STATE = BluetoothDevice.EXTRA_BOND_STATE
    }
}