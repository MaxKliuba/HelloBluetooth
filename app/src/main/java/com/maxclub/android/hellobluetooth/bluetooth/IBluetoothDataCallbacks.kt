package com.maxclub.android.hellobluetooth.bluetooth

import androidx.lifecycle.LiveData
import com.maxclub.android.hellobluetooth.model.Command

interface IBluetoothDataCallbacks {
    fun onSend(data: String)

    fun onCommandListener(): LiveData<Command>
}