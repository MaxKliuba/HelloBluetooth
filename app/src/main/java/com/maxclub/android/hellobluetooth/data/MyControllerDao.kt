package com.maxclub.android.hellobluetooth.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface MyControllerDao {
    /*
     * Controller
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertControllers(vararg controllers: Controller)

    @Update
    suspend fun updateControllers(vararg controllers: Controller)

    @Delete
    suspend fun deleteControllers(vararg controllers: Controller)

    /*
     * Widget
     */
    @Query("SELECT * FROM widget_table WHERE controller_id = :controllerId")
    fun getWidgets(controllerId: UUID): LiveData<List<Widget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgets(vararg widgets: Widget)

    @Update
    suspend fun updateWidgets(vararg widgets: Widget)

    @Delete
    suspend fun deleteWidgets(vararg widgets: Widget)

    /*
     * Controller with Widgets
     */
    @Transaction
    @Query("SELECT * FROM controller_table  WHERE id = :controllerId")
    fun getControllerWithWidgets(controllerId: UUID): LiveData<ControllerWithWidgets>

    @Transaction
    @Query("SELECT * FROM controller_table")
    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>>

    suspend fun insertControllerWithWidgets(controllerWithWidgets: ControllerWithWidgets) {
        insertControllers(controllerWithWidgets.controller)
        insertWidgets(*controllerWithWidgets.widgets.toTypedArray())
    }
}