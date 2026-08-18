package com.mobileinvalley.journeypal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: JourneyItem)

    @Update
    suspend fun updateItem(item: JourneyItem)

    @Delete
    suspend fun deleteItem(item: JourneyItem)

    @Query("SELECT * FROM journey_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<JourneyItem>>

    @Query("SELECT * FROM journey_items WHERE id = :id")
    fun getItemById(id: String): Flow<JourneyItem?>

    @Query("SELECT * FROM journey_items WHERE notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchItems(query: String): Flow<List<JourneyItem>>

    @Query("SELECT * FROM journey_items WHERE timestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY timestamp DESC")
    fun getItemsInDateRange(startTimestamp: kotlinx.datetime.Instant, endTimestamp: kotlinx.datetime.Instant): Flow<List<JourneyItem>>
}
