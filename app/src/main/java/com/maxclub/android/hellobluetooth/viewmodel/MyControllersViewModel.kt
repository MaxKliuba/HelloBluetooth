package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.ControllerDatabase
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.repository.ControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyControllersViewModel(application: Application) : AndroidViewModel(application) {
    private val controllerRepository: ControllerRepository
    var isDragging: Boolean = false

    init {
        val controllerDao = ControllerDatabase.getDatabase(application).controllerDao()
        controllerRepository = ControllerRepository(controllerDao)
    }

    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>> =
        controllerRepository.getControllersWithWidgets()

    fun deleteController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            controllerRepository.deleteController(controller)
        }
    }
}