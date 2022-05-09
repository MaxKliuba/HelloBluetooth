package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import com.maxclub.android.hellobluetooth.repository.WidgetIconRepository
import com.maxclub.android.hellobluetooth.model.WidgetIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository
    val widgetIcons: List<WidgetIcon> = WidgetIconRepository.widgetIcons
    var isValuesUpdated = false
    var selectedType: Widget.Type? = null
    var selectedWidgetIcon: WidgetIcon? = null

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