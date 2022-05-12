package com.maxclub.android.hellobluetooth.data

import androidx.annotation.StringRes
import androidx.room.*
import com.maxclub.android.hellobluetooth.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
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
    @Transient
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),

    @Transient
    @ColumnInfo(name = "controller_id", index = true)
    var controllerId: UUID? = null,

    @SerialName("name")
    @ColumnInfo(name = "name")
    var name: String = "New Widget",

    @Serializable(with = WidgetTypeSerializer::class)
    @SerialName("type")
    @ColumnInfo(name = "type")
    var type: Type,

    @SerialName("tag")
    @ColumnInfo(name = "tag")
    var tag: String,

    @SerialName("icon_id")
    @ColumnInfo(name = "icon_res_id")
    var iconResId: Int,

    @SerialName("readonly")
    @ColumnInfo(name = "readonly")
    var isReadOnly: Boolean,

    @SerialName("order")
    @ColumnInfo(name = "order")
    var order: Int = -1,
) : java.io.Serializable {
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
}