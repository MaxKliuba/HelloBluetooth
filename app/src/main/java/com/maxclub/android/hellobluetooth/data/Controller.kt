package com.maxclub.android.hellobluetooth.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "controller_table")
data class Controller(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "New Controller",
    var order: Int = 0,
) : Serializable
