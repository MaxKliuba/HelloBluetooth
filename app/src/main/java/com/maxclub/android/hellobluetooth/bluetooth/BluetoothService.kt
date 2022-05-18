package com.maxclub.android.hellobluetooth.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.receivers.BluetoothStateReceiver
import com.maxclub.android.hellobluetooth.receivers.BluetoothTransferReceiver
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

private const val LOG_TAG = "BluetoothService"

class BluetoothService(private val context: Context) {
    private val manager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val adapter: BluetoothAdapter = manager.adapter
    private lateinit var socket: BluetoothSocket
    val isSocketConnected: Boolean
        get() = ::socket.isInitialized && socket.isConnected
    var isListening: Boolean = false
        private set
    var device: BluetoothDevice? = null
        private set

    private val mutableState: MutableLiveData<Int> = MutableLiveData()
    val state: LiveData<Int> = Transformations.switchMap(mutableState) {
        MutableLiveData(it)
    }

    fun updateState(state: Int) {
        this.mutableState.value = state
    }

    fun send(data: String) {
        try {
            socket.outputStream.write(data.toByteArray() + terminalBytes)
            context.sendBroadcast(
                Intent().apply {
                    action = BluetoothTransferReceiver.ACTION_DATA_SENT
                    putExtra(BluetoothTransferReceiver.EXTRA_DATA, data)
                }
            )
        } catch (e: Exception) {
            val message = context.getString(R.string.send_error_message)
            Log.e(LOG_TAG, message, e)
            context.sendBroadcast(
                Intent().apply {
                    action = BluetoothTransferReceiver.ACTION_ERROR
                    putExtra(BluetoothTransferReceiver.EXTRA_DATA, data)
                    putExtra(BluetoothTransferReceiver.EXTRA_ERROR, message)
                }
            )
        }
    }

    suspend fun startListening() {
        if (!isListening && state.value == BluetoothAdapter.STATE_CONNECTED) {
            Log.i(LOG_TAG, "START LISTENING")
            isListening = true

            withContext(Dispatchers.IO) {
                while (isListening && state.value == BluetoothAdapter.STATE_CONNECTED && !isSocketConnected) {
                    delay(100)
                }

                var buffer = ByteArray(socket.maxReceivePacketSize)
                var size = 0
                while (isListening && isSocketConnected) {
                    try {
                        if (socket.inputStream.available() > 0 && size < buffer.size) {
                            val byte = socket.inputStream.read().toByte()
                            if (!terminalBytes.contains(byte)) {
                                buffer[size] = byte
                                size++
                            } else {
                                val data = String(buffer, 0, size).trim()
                                if (data.isNotEmpty()) {
                                    context.sendBroadcast(
                                        Intent().apply {
                                            action = BluetoothTransferReceiver.ACTION_DATA_RECEIVED
                                            putExtra(BluetoothTransferReceiver.EXTRA_DATA, data)
                                        }
                                    )
                                }
                                buffer = ByteArray(socket.maxReceivePacketSize)
                                size = 0
                            }
                        }
                    } catch (e: Exception) {
                        val message = context.getString(R.string.receive_error_message)
                        Log.e(LOG_TAG, message, e)
                    }
                    delay(1)
                }
            }
            stopListening()
        }
    }

    fun stopListening() {
        if (isListening) {
            Log.i(LOG_TAG, "STOP LISTENING")
            isListening = false
        }
    }

    suspend fun connect(device: BluetoothDevice) {
        val bluetoothManager: BluetoothManager =
            context.getSystemService(BluetoothManager::class.java)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothManager.adapter.cancelDiscovery()
        }
        closeConnection()

        this.device = device
        context.sendBroadcast(
            Intent().apply {
                action = BluetoothStateReceiver.ACTION_DEVICE_CONNECTING
                putExtra(BluetoothStateReceiver.EXTRA_DEVICE, device)
            }
        )

        withContext(Dispatchers.IO) {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            try {
                socket.connect()
            } catch (e: IOException) {
                val message = context.getString(R.string.connecting_error_message)
                Log.e(LOG_TAG, message, e)
                context.sendBroadcast(
                    Intent().apply {
                        action = BluetoothStateReceiver.ACTION_CONNECTION_ERROR
                        putExtra(BluetoothStateReceiver.EXTRA_DEVICE, device)
                        putExtra(BluetoothStateReceiver.EXTRA_ERROR, message)
                    }
                )
            }
        }
    }

    fun disconnect() {
        context.sendBroadcast(
            Intent().apply {
                action = BluetoothStateReceiver.ACTION_DEVICE_DISCONNECTING
                putExtra(BluetoothStateReceiver.EXTRA_DEVICE, device)
            }
        )
        closeConnection()
    }

    fun closeConnection() {
        try {
            if (isSocketConnected) {
                socket.close()
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Failed to close socket connection", e)
        }
    }

    companion object {
        private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val terminalBytes = byteArrayOf(0x0d, 0x0a) // '\r', '\n'
    }
}