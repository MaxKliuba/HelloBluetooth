package com.maxclub.android.hellobluetooth.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ControllerWithWidgets(
    @SerialName("c")
    @Embedded
    val controller: Controller,

    @SerialName("w")
    @Relation(
        entity = Widget::class,
        parentColumn = "id",
        entityColumn = "controller_id",
    )
    val widgets: List<Widget>,
) : Parcelable {
    init {
        widgets.forEach {
            it.controllerId = controller.id
        }
    }
}
