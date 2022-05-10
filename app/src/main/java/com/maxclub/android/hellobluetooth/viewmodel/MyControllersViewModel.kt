package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyControllersViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository
    var isDragged: Boolean = false

    init {
        val myControllerDao = MyControllerDatabase.getDatabase(application).myControllerDao()
        myControllerRepository = MyControllerRepository(myControllerDao)
    }

    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>> =
        myControllerRepository.getControllersWithWidgets()

    fun updateControllers(vararg controllers: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.updateControllers(*controllers)
        }
    }

    fun deleteController(controller: Controller) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.deleteControllers(controller)
        }
    }
}