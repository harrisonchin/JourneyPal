package com.mobileinvalley.journeypal.pro

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
        create = true,
        error = null
    )
    val path = documentDirectory?.path
    if (path != null && !fileManager.fileExistsAtPath(path)) {
        fileManager.createDirectoryAtPath(path, withIntermediateDirectories = true, attributes = null, error = null)
    }
    
    val dbFilePath = (path ?: "") + "/journey.db"
    return Room.databaseBuilder<JourneyDatabase>(
        name = dbFilePath,
        factory = { JourneyDatabaseConstructor.initialize() }
    )
}
