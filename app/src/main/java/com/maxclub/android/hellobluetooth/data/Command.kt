package com.maxclub.android.hellobluetooth.data

import java.util.*

data class Command(
    val type: Int,
    val text: String,
    val time: Date,
    val isSuccess: Boolean = true,
) {
    companion object {
        const val INPUT_COMMAND = 0
        const val OUTPUT_COMMAND = 1
    }
}
