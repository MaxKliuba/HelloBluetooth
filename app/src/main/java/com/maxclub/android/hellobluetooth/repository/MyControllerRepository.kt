package com.maxclub.android.hellobluetooth.repository

import androidx.lifecycle.LiveData
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.MyControllerDao
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.data.Widget
import java.util.*

class MyControllerRepository(private val controllerDao: MyControllerDao) {
    fun getWidgets(controllerId: UUID): LiveData<List<Widget>> =
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