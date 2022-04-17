package com.maxclub.android.hellobluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val LOG_TAG = "BluetoothService"

object BluetoothService {
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var socket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream

    val isConnected: Boolean
        get() = ::socket.isInitialized && socket.isConnected

    var processDevice: BluetoothDevice? = null
        private set

    val device: BluetoothDevice?
        get() = if (isConnected) socket.remoteDevice else null

    private val mutableState: MutableLiveData<Int> = MutableLiveData()
    val state: LiveData<Int> = Transformations.switchMap(mutableState) {
        MutableLiveData(it)
    }

    fun updateState(state: Int) {
        this.mutableState.value = state
    }

    fun write(bytes: ByteArray) {
        outputStream.write(bytes)
        // TODO
    }

    fun write(data: String) {
        write(data.toByteArray())
    }

    suspend fun listen() {
        withContext(Dispatchers.IO) {
            // TODO
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(context: Context, device: BluetoothDevice) {
        val bluetoothManager: BluetoothManager =
            context.getSystemService(BluetoothManager::class.java)
        bluetoothManager.adapter.cancelDiscovery()
        closeConnection(context)

        processDevice = device
        context.sendBroadcast(
            Intent().apply {
                action = BluetoothStateBroadcastReceiver.ACTION_DEVICE_CONNECTING
                putExtra(BluetoothStateBroadcastReceiver.EXTRA_DEVICE, processDevice)
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
                        action = BluetoothStateBroadcastReceiver.ACTION_CONNECTION_ERROR
                        putExtra(BluetoothStateBroadcastReceiver.EXTRA_DEVICE, processDevice)
                    }
                )
            }
        }
    }

    fun disconnect(context: Context) {
        context.sendBroadcast(
            Intent().apply {
                action = BluetoothStateBroadcastReceiver.ACTION_DEVICE_DISCONNECTING
                putExtra(BluetoothStateBroadcastReceiver.EXTRA_DEVICE, processDevice)
            }
        )
        closeConnection(context)
    }

    fun closeConnection(context: Context) {
        try {
            if (isConnected) {
                socket.close()
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not close the connect socket", e)
            context.sendBroadcast(Intent().apply {
                action = BluetoothStateBroadcastReceiver.ACTION_CONNECTION_ERROR
                putExtra(BluetoothStateBroadcastReceiver.EXTRA_DEVICE, processDevice)
            })
        }
    }
}