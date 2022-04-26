package com.maxclub.android.hellobluetooth.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.maxclub.android.hellobluetooth.data.Command

object CommandRepository {
    private val mutableCommands: MutableLiveData<List<Command>> = MutableLiveData(emptyList())
    val commands: LiveData<List<Command>> = Transformations.switchMap(mutableCommands) {
        MutableLiveData(it)
    }

    fun addCommand(command: Command) {
        mutableCommands.value = mutableCommands.value?.plus(command)
    }

    fun clearCommands() {
        mutableCommands.value = emptyList()
    }
}