package com.mobileinvalley.journeypal

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<JourneyDatabase> {
    val fileManager = NSFileManager.defaultManager
    val documentDirectory = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbFilePath = documentDirectory!!.path + "/journey.db"
    return Room.databaseBuilder<JourneyDatabase>(
        name = dbFilePath,
        factory = { JourneyDatabaseConstructor.initialize() }
    )
}
