package com.maxclub.android.hellobluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val LOG_TAG = "BluetoothService"

class BluetoothService private constructor(context: Context) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    var bluetoothDevice: BluetoothDevice? = null
    private lateinit var socket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream

    val connectionState = MutableLiveData(bluetoothAdapter.state)

    fun refreshConnectionState() {
        if (!bluetoothAdapter.isEnabled) {
            connectionState.value = bluetoothAdapter.state
        }
    }

    fun write(bytes: ByteArray) {
        outputStream.write(bytes)
    }

    fun write(data: String) {
        write(data.toByteArray())
    }

    suspend fun listen() {
        withContext(Dispatchers.IO) {

        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(context: Context, device: BluetoothDevice) {
        bluetoothAdapter.cancelDiscovery()
        bluetoothDevice = device
        closeConnection(context)

        context.sendBroadcast(
            Intent().apply {
                action = ACTION_ACL_CONNECTING
                putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
            }
        )

        withContext(Dispatchers.IO) {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            try {
                socket.connect()
                outputStream = socket.outputStream
                inputStream = socket.inputStream
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Socket's connect() method failed", e)
                context.sendBroadcast(
                    Intent().apply {
                        action = ACTION_CONNECTION_ERROR
                        putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
                    }
                )
            }
        }
    }

    fun disconnect(context: Context) {
        context.sendBroadcast(
            Intent().apply {
                action = ACTION_ACL_DISCONNECTING
                putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
            }
        )
        closeConnection(context)
    }

    fun closeConnection(context: Context) {
        try {
            if (::socket.isInitialized && socket.isConnected) {
                socket.close()
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not close the connect socket", e)
            context.sendBroadcast(Intent().apply {
                action = ACTION_CONNECTION_ERROR
                putExtra(BluetoothDevice.EXTRA_DEVICE, bluetoothDevice)
            })
        }
    }

    companion object {
        const val ACTION_ACL_CONNECTING = "com.maxclub.android.hellobluetooth.action.ACL_CONNECTING"
        const val ACTION_ACL_DISCONNECTING =
            "om.maxclub.android.hellobluetooth.action.ACL_DISCONNECTING"
        const val ACTION_CONNECTION_ERROR =
            "om.maxclub.android.hellobluetooth.action.CONNECTION_ERROR"

        private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        private var instance: BluetoothService? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = BluetoothService(context)
            }
        }

        fun get(): BluetoothService =
            instance ?: throw IllegalStateException("BluetoothRepository must be initialized")
    }
}