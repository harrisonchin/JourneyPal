package com.mobileinvalley.journeypal

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<JourneyDatabase> {
    val dbFilePath = NSHomeDirectory() + "/journey.db"
    return Room.databaseBuilder<JourneyDatabase>(
        name = dbFilePath,
        factory = { JourneyDatabaseConstructor.initialize() }
    )
}
