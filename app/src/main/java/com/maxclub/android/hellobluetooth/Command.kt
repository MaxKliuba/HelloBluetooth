package com.maxclub.android.hellobluetooth

import java.util.*

data class Command(val type: Int, val text: String, val time: Date) {
    companion object {
        const val INPUT_COMMAND = 0
        const val OUTPUT_COMMAND = 1
    }
}
