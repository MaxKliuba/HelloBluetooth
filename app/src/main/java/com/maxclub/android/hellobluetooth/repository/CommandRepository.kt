package com.maxclub.android.hellobluetooth.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.maxclub.android.hellobluetooth.model.Command

object CommandRepository {
    private val mutableCommands: MutableList<Command> = mutableListOf()
    val commands: List<Command> = mutableCommands

    private val mutableCommand: MutableLiveData<Command> = MutableLiveData(null)
    val command: LiveData<Command> = Transformations.switchMap(mutableCommand) {
        MutableLiveData(it)
    }

    fun addCommand(command: Command) {
        mutableCommands += command
        mutableCommand.value = command
    }

    fun clearCommands() {
        mutableCommands.clear()
        mutableCommand.value = null
    }
}