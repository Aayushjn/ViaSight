package com.aayush.viasight.util.common

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Serializer(Date::class)
object DateSerializer: KSerializer<Date> {
    private val dateFormat: DateFormat = SimpleDateFormat.getDateTimeInstance()

    override val descriptor: SerialDescriptor = StringDescriptor

    override fun serialize(encoder: Encoder, obj: Date) = encoder.encodeString(dateFormat.format(obj))

    override fun deserialize(decoder: Decoder): Date = dateFormat.parse(decoder.decodeString())!!
}