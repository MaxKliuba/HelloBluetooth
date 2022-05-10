package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ControllerSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository

    init {
        val myControllerDao = MyControllerDatabase.getDatabase(application).myControllerDao()
        myControllerRepository = MyControllerRepository(myControllerDao)
    }

    fun insertController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.insertControllers(controller)
        }
    }

    fun updateController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.updateControllers(controller)
        }
    }
}