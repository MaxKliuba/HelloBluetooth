package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.Command
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.repository.CommandRepository
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository
    var isDragging: Boolean = false

    init {
        val myControllerDao = MyControllerDatabase.getDatabase(application).myControllerDao()
        myControllerRepository = MyControllerRepository(myControllerDao)
    }

    fun getWidgets(controllerId: Int): LiveData<List<Widget>> =
        myControllerRepository.getWidgets(controllerId)

    fun deleteWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.deleteWidget(widget)
        }
    }

    fun getCommands(): List<Command> = CommandRepository.commands
}