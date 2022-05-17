package com.maxclub.android.hellobluetooth.data

import com.maxclub.android.hellobluetooth.repository.WidgetIconRepository
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class WidgetTypeSerializer : KSerializer<Widget.Type> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Type", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Widget.Type =
        Widget.Type.values().getOrNull(decoder.decodeInt()) ?: Widget.Type.TEXT_FIELD

    override fun serialize(encoder: Encoder, value: Widget.Type) {
        encoder.encodeInt(value.ordinal)
    }
}

class BooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Boolean", PrimitiveKind.BYTE)

    override fun deserialize(decoder: Decoder): Boolean =
        decoder.decodeByte() != 0.toByte()

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeByte(if (!value) 0 else 1)
    }
}

class IconResSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Int", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int =
        WidgetIconRepository.widgetIconMap[decoder.decodeInt()]?.drawableResId ?: 0

    override fun serialize(encoder: Encoder, value: Int) {
        val widgetIcon = WidgetIconRepository.widgetIcons.firstOrNull { it.drawableResId == value }
        val widgetIconId =
            WidgetIconRepository.widgetIconMap.filterValues { it == widgetIcon }.keys.firstOrNull()
                ?: 0
        encoder.encodeInt(widgetIconId)
    }
}