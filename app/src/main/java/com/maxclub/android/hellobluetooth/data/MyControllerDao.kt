package com.maxclub.android.hellobluetooth.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface MyControllerDao {
    @Query("SELECT * FROM widget_table WHERE controller_id = :controllerId")
    fun getWidgets(controllerId: UUID): LiveData<List<Widget>>

    @Transaction
    @Query("SELECT * FROM controller_table")
    fun getControllersWithWidgets(): LiveData<List<ControllerWithWidgets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addController(controller: Controller)

    @Update
    suspend fun updateController(controller: Controller)

    @Delete
    suspend fun deleteController(controller: Controller)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWidget(widget: Widget)

    @Update
    suspend fun updateWidget(widget: Widget)

    @Delete
    suspend fun deleteWidget(widget: Widget)
}