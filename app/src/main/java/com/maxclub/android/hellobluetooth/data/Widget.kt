package com.maxclub.android.hellobluetooth.data

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.maxclub.android.hellobluetooth.R
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
    var type: Type,
    var size: Size,
    var order: Int = 0,
) : Serializable {
    enum class Type(@StringRes val titleResId: Int) {
        BUTTON(R.string.widget_type_button_title),
        SWITCH(R.string.widget_type_switch_title),
    }

    enum class Size(
        @StringRes val titleResId: Int,
        val value: Int
    ) {
        SMALL(R.string.widget_size_small_title, 1),
        LARGE(R.string.widget_size_large_title, 2),
    }
}