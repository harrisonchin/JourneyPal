package com.mobileinvalley.journeypal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JourneyItem)

    @Query("SELECT * FROM journey_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<JourneyItem>>
}
