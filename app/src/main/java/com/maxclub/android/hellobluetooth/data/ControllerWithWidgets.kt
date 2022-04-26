package com.maxclub.android.hellobluetooth.data

import androidx.room.Embedded
import androidx.room.Relation

data class ControllerWithWidgets(
    @Embedded val controller: Controller,
    @Relation(
        entity = Widget::class,
        parentColumn = "id",
        entityColumn = "controller_id",
    ) val widgets: List<Widget>,
)
