package com.maxclub.android.hellobluetooth.repository

import androidx.lifecycle.LiveData
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.MyControllerDao
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.data.Widget
import java.util.*

class MyControllerRepository(private val controllerDao: MyControllerDao) {
    /*
     * Controller
     */
    suspend fun insertControllers(vararg controllers: Controller) {
        controllerDao.insertControllers(*controllers)
    }

    suspend fun updateControllers(vararg controllers: Controller) {
        controllerDao.updateControllers(*controllers)
    }

    suspend fun deleteControllers(vararg controllers: Controller) {
        controllerDao.deleteControllers(*controllers)
    }

    /*
     * Widget
     */
    fun getWidgets(controllerId: UUID): LiveData<List<Widget>> =
        controllerDao.getWidgets(controllerId)

    suspend fun insertWidgets(vararg widgets: Widget) {
        controllerDao.insertWidgets(*widgets)
    }

    suspend fun updateWidgets(vararg widgets: Widget) {
        controllerDao.updateWidgets(*widgets)
    }

    suspend fun deleteWidgets(vararg widgets: Widget) {
        controllerDao.deleteWidgets(*widgets)
    }

    /*
     * Controller with Widgets
     */
    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>> =
        controllerDao.getControllersWithWidgets()

    suspend fun insertControllerWithWidgets(controllerWithWidgets: ControllerWithWidgets) {
        controllerDao.insertControllerWithWidgets(controllerWithWidgets)
    }
}