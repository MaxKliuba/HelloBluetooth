package com.maxclub.android.hellobluetooth.data

import androidx.room.TypeConverter

class WidgetTypeConverters {
    @TypeConverter
    fun fromType(type: Widget.Type?): Int? = type?.ordinal

    @TypeConverter
    fun toType(ordinal: Int?): Widget.Type? = ordinal?.let { Widget.Type.values()[it] }

    @TypeConverter
    fun fromSize(size: Widget.Size?): Int? = size?.ordinal

    @TypeConverter
    fun toSize(ordinal: Int?): Widget.Size? = ordinal?.let { Widget.Size.values()[it] }
}