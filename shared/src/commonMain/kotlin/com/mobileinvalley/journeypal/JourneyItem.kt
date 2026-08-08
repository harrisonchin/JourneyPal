package com.mobileinvalley.journeypal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "journey_items")
data class JourneyItem(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "photo_uris")
    val photoUris: List<String> = emptyList(),
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val notes: String,
)
