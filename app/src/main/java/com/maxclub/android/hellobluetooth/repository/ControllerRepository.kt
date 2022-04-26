package com.maxclub.android.hellobluetooth.repository

import androidx.lifecycle.LiveData
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.ControllerDao
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.data.Widget

class ControllerRepository(private val controllerDao: ControllerDao) {
    fun getWidgets(controllerId: Int): LiveData<List<Widget>> =
        controllerDao.getWidgets(controllerId)

    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>> =
        controllerDao.getControllersWithWidgets()

    suspend fun addController(controller: Controller) {
        controllerDao.addController(controller)
    }

    suspend fun updateController(controller: Controller) {
        controllerDao.updateController(controller)
    }

    suspend fun deleteController(controller: Controller) {
        controllerDao.deleteController(controller)
    }

    suspend fun addWidget(widget: Widget) {
        controllerDao.addWidget(widget)
    }

    suspend fun updateWidget(widget: Widget) {
        controllerDao.updateWidget(widget)
    }

    suspend fun deleteWidget(widget: Widget) {
        controllerDao.deleteWidget(widget)
    }
}