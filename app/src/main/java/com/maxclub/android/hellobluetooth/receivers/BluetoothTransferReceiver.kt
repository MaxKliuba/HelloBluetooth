package com.maxclub.android.hellobluetooth.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.maxclub.android.hellobluetooth.R

private const val LOG_TAG = "BluetoothTransferReceiver"

class BluetoothTransferReceiver : BroadcastReceiver() {
    interface Callbacks {
        fun onSent(data: String)

        fun onReceived(data: String)

        fun onFailure(data: String, message: String)
    }

    private var callbacks: Callbacks? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DATA_SENT -> {
                val data = intent.getStringExtra(EXTRA_DATA) ?: ""
                Log.i(LOG_TAG, "ACTION_DATA_SENT -> $data")
                callbacks?.onSent(data)
            }
            ACTION_DATA_RECEIVED -> {
                val data = intent.getStringExtra(EXTRA_DATA) ?: ""
                Log.i(LOG_TAG, "ACTION_DATA_RECEIVED -> $data")
                callbacks?.onReceived(data)
            }
            ACTION_ERROR -> {
                val data = intent.getStringExtra(EXTRA_DATA) ?: ""
                val message = intent.getStringExtra(EXTRA_ERROR)
                    ?: context.getString(R.string.some_error_message)
                Log.i(LOG_TAG, "ACTION_ERROR -> $message")
                callbacks?.onFailure(data, message)
            }
            else -> {
                Log.i(LOG_TAG, "UNKNOWN_ACTION -> ${intent.action}")
            }
        }
    }

    fun register(context: Context, listener: Callbacks) {
        val filter = IntentFilter().apply {
            addAction(ACTION_DATA_SENT)
            addAction(ACTION_DATA_RECEIVED)
            addAction(ACTION_ERROR)
        }
        context.registerReceiver(this, filter)
        callbacks = listener
    }

    fun unregister(context: Context) {
        callbacks = null
        context.unregisterReceiver(this)
    }

    companion object {
        const val ACTION_DATA_SENT = "com.maxclub.android.hellobluetooth.action.DATA_SENT"
        const val ACTION_DATA_RECEIVED = "com.maxclub.android.hellobluetooth.action.DATA_RECEIVED"
        const val ACTION_ERROR = "com.maxclub.android.hellobluetooth.action.ERROR"

        const val EXTRA_DATA = "com.maxclub.android.hellobluetooth.extra.DATA"
        const val EXTRA_ERROR = "com.maxclub.android.hellobluetooth.extra.ERROR"
    }
}