package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.ControllerDatabase
import com.maxclub.android.hellobluetooth.repository.ControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControllerSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val controllerRepository: ControllerRepository

    init {
        val controllerDao = ControllerDatabase.getDatabase(application).controllerDao()
        controllerRepository = ControllerRepository(controllerDao)
    }

    fun addController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            controllerRepository.addController(controller)
        }
    }

    fun updateController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            controllerRepository.updateController(controller)
        }
    }
}