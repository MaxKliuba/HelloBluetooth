package com.maxclub.android.hellobluetooth.data

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
        Widget.Type.values()[decoder.decodeInt()]

    override fun serialize(encoder: Encoder, value: Widget.Type) {
        encoder.encodeInt(value.ordinal)
    }
}