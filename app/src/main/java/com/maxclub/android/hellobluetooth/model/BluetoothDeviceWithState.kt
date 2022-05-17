package com.maxclub.android.hellobluetooth.model

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceWithState(val device: BluetoothDevice, var state: Int)