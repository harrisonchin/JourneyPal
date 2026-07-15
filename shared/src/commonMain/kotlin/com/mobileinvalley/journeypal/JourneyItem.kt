package com.mobileinvalley.journeypal

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "journey_items")
data class JourneyItem(
    @PrimaryKey
    val id: String,
    val photoPath: String,
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val notes: String,
)
