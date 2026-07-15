package com.mobileinvalley.journeypal

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(entities = [JourneyItem::class], version = 1)
@TypeConverters(JourneyConverters::class)
@ConstructedBy(JourneyDatabaseConstructor::class)
abstract class JourneyDatabase : RoomDatabase() {
    abstract fun journeyDao(): JourneyDao
}

// The Room compiler will generate an implementation of this.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object JourneyDatabaseConstructor : RoomDatabaseConstructor<JourneyDatabase> {
    override fun initialize(): JourneyDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<JourneyDatabase>

fun getDatabase(
    builder: RoomDatabase.Builder<JourneyDatabase>
): JourneyDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .build()
}

fun provideDatabase(): JourneyDatabase {
    return getDatabase(getDatabaseBuilder())
}
