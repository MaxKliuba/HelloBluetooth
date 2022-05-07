package com.maxclub.android.hellobluetooth.utils

class CommandHelper {
    companion object {
        const val TAG_TERMINATOR = "#"
        const val SYNC_TAG = "sync"
        const val LOW_VALUE = "0"
        const val HIGH_VALUE = "1"

        fun create(tag: String, data: String): String = "$tag$TAG_TERMINATOR$data"

        fun parse(textCommand: String): Pair<String, String> {
            val tag = textCommand.substringBefore(TAG_TERMINATOR)
            val data = textCommand.substringAfter(TAG_TERMINATOR)
            return tag to data
        }
    }
}