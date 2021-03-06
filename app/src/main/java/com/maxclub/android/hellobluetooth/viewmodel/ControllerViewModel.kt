package com.maxclub.android.hellobluetooth.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.maxclub.android.hellobluetooth.model.Command
import com.maxclub.android.hellobluetooth.data.MyControllerDatabase
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.repository.CommandRepository
import com.maxclub.android.hellobluetooth.repository.MyControllerRepository
import com.maxclub.android.hellobluetooth.repository.WidgetIconRepository
import com.maxclub.android.hellobluetooth.model.WidgetIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ControllerViewModel(application: Application) : AndroidViewModel(application) {
    private val myControllerRepository: MyControllerRepository
    private val widgetIcons: List<WidgetIcon> = WidgetIconRepository.widgetIcons
    var currentVoiceWidget: Widget? = null
    var isDragged: Boolean = false
    var tempList: List<Widget> = emptyList()

    init {
        val myControllerDao = MyControllerDatabase.getDatabase(application).myControllerDao()
        myControllerRepository = MyControllerRepository(myControllerDao)
    }

    fun getWidgets(controllerId: UUID): LiveData<List<Widget>> =
        myControllerRepository.getWidgets(controllerId)

    fun updateWidgets(vararg widgets: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.updateWidgets(*widgets)
        }
    }

    fun deleteWidget(widget: Widget) {
        viewModelScope.launch(Dispatchers.IO) {
            myControllerRepository.deleteWidgets(widget)
        }
    }

    fun getCommands(): List<Command> = CommandRepository.commands

    fun isWidgetIconResIdValid(@DrawableRes drawableResId: Int): Boolean =
        drawableResId > 0 && widgetIcons.any { it.drawableResId == drawableResId && it.isValid }
}