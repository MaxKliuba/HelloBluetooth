package com.maxclub.android.hellobluetooth.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Parcelize
@Serializable
@Entity(tableName = "controller_table")
data class Controller(
    @Transient
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),

    @SerialName("n")
    @ColumnInfo(name = "name")
    var name: String = "New Controller",

    @SerialName("o")
    @ColumnInfo(name = "order")
    var order: Int = -1,
) : Parcelable
