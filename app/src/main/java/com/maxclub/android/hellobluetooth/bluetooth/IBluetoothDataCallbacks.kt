package com.maxclub.android.hellobluetooth.bluetooth

import androidx.lifecycle.LiveData
import com.maxclub.android.hellobluetooth.data.Command

interface IBluetoothDataCallbacks {
    fun onSend(data: String)

    fun getCommands(): LiveData<List<Command>>
}