package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.ControllerDatabase
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.repository.ControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val controllerRepository: ControllerRepository
    var isDragging: Boolean = false

    init {
        val controllerDao = ControllerDatabase.getDatabase(application).controllerDao()
        controllerRepository = ControllerRepository(controllerDao)
    }

    fun getWidgets(controllerId: Int): LiveData<List<Widget>> =
        controllerRepository.getWidgets(controllerId)

    fun addWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            controllerRepository.addWidget(widget)
        }
    }

    fun deleteWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            controllerRepository.deleteWidget(widget)
        }
    }
}