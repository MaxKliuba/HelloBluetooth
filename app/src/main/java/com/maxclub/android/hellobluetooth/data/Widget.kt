package com.maxclub.android.hellobluetooth.data

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.*
import com.maxclub.android.hellobluetooth.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Parcelize
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

    @SerialName("n")
    @ColumnInfo(name = "name")
    var name: String = "New Widget",

    @Serializable(with = WidgetTypeSerializer::class)
    @SerialName("t")
    @ColumnInfo(name = "type")
    var type: Type,

    @SerialName("tg")
    @ColumnInfo(name = "tag")
    var tag: String,

    @Serializable(with = IconResSerializer::class)
    @SerialName("i")
    @ColumnInfo(name = "icon_res_id")
    var iconResId: Int,

    @Serializable(with = BooleanSerializer::class)
    @SerialName("r")
    @ColumnInfo(name = "readonly")
    var isReadOnly: Boolean,

    @SerialName("o")
    @ColumnInfo(name = "order")
    var order: Int = -1,
) : Parcelable {
    @IgnoredOnParcel
    @Ignore
    var desiredState: String? = null

    @IgnoredOnParcel
    @Ignore
    var state: String? = null

    enum class Type(
        @DrawableRes val drawableResId: Int,
        @StringRes val titleResId: Int
    ) {
        BUTTON(R.drawable.widget_types__button, R.string.widget_types__button),
        SWITCH(R.drawable.widget_types__switch, R.string.widget_types__switch),
        SLIDER(R.drawable.widget_types__slider, R.string.widget_types__slider),
        TEXT_FIELD(R.drawable.widget_types__text_field, R.string.widget_types__text_field),
        VOICE_BUTTON(R.drawable.widget_icons__mic, R.string.widget_types__voice_button),
    }
}