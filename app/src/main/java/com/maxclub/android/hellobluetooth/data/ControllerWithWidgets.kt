package com.maxclub.android.hellobluetooth.data

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
) : java.io.Serializable {
    init {
        widgets.forEach {
            it.controllerId = controller.id
        }
    }
}
