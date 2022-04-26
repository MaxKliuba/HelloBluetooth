package com.maxclub.android.hellobluetooth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "widget_table",
    foreignKeys = [ForeignKey(
        entity = Controller::class,
        parentColumns = ["id"],
        childColumns = ["controller_id"],
        onDelete = ForeignKey.CASCADE,
    )]
)
data class Widget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "controller_id") val controllerId: Int,
    var name: String = "New Widget",
    var type: Int,
    var size: Int,
    var order: Int = 0,
) : Serializable {
    companion object {
        // TYPE
        const val BUTTON_TYPE = 0
        const val SWITCH_TYPE = 1
        // TODO

        // SIZE
        const val SMALL_SIZE = 1
        const val LARGE_SIZE = 2
    }
}