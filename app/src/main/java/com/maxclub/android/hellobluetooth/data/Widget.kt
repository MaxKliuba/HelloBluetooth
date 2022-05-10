package com.maxclub.android.hellobluetooth.data

import androidx.annotation.StringRes
import androidx.room.*
import com.maxclub.android.hellobluetooth.R
import java.io.Serializable
import java.util.*

@Entity(
    tableName = "widget_table",
    foreignKeys = [ForeignKey(
        entity = Controller::class,
        parentColumns = ["id"],
        childColumns = ["controller_id"],
        onDelete = ForeignKey.CASCADE,
    )],
)
data class Widget(
    @PrimaryKey @ColumnInfo(name = "id") val id: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "controller_id", index = true) val controllerId: UUID,
    @ColumnInfo(name = "name") var name: String = "New Widget",
    @ColumnInfo(name = "type") var type: Type,
    @ColumnInfo(name = "size") var size: Size,
    @ColumnInfo(name = "tag") var tag: String,
    @ColumnInfo(name = "icon_res_id") var iconResId: Int,
    @ColumnInfo(name = "readonly") var isReadOnly: Boolean,
    @ColumnInfo(name = "order") var order: Int = -1,
) : Serializable {
    @Ignore
    var desiredState: String? = null

    @Ignore
    var state: String? = null


    enum class Type(@StringRes val titleResId: Int) {
        BUTTON(R.string.widget_type_button_title),
        SWITCH(R.string.widget_type_switch_title),
        SLIDER(R.string.widget_type_slider_title),
        TEXT_FIELD(R.string.widget_type_text_field_title),
        VOICE_BUTTON(R.string.widget_type_voice_button_title),
    }

    enum class Size(
        @StringRes val titleResId: Int,
        val value: Int
    ) {
        SMALL(R.string.widget_size_small_title, 1),
        LARGE(R.string.widget_size_large_title, 2),
    }
}