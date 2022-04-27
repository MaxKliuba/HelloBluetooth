package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository

    var selectedTypeId = -1
    var selectedSizeId = -1

    init {
        val myControllerDao = MyControllerDatabase.getDatabase(application).myControllerDao()
        myControllerRepository = MyControllerRepository(myControllerDao)
    }

    fun addWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.addWidget(widget)
        }
    }

    fun updateWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.updateWidget(widget)
        }
    }
}