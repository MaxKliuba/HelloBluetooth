package com.maxclub.android.hellobluetooth.viewmodel

import androidx.lifecycle.ViewModel
import com.maxclub.android.hellobluetooth.data.Command
import com.maxclub.android.hellobluetooth.repository.CommandRepository

class TerminalViewModel : ViewModel() {
    fun getCommands(): List<Command> = CommandRepository.commands

    fun clearCommands() {
        CommandRepository.clearCommands()
    }
}