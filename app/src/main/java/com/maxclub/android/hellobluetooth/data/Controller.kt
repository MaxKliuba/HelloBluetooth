package com.maxclub.android.hellobluetooth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "controller_table")
data class Controller(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") var name: String = "New Controller",
    @ColumnInfo(name = "order") var order: Int = -1,
) : Serializable
