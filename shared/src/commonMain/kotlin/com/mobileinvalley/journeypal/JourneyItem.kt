package com.mobileinvalley.journeypal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

@Entity(tableName = "journey_items")
@Serializable
data class JourneyItem(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "photo_uris")
    val photoUris: List<String> = emptyList(),
    val timestamp: @Serializable(with = InstantSerializer::class) Instant,
    val latitude: Double,
    val longitude: Double,
    val notes: String,
)
