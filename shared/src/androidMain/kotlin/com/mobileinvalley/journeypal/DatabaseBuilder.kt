package com.mobileinvalley.journeypal

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private lateinit var appContext: Context

fun initDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<JourneyDatabase> {
    val dbFile = appContext.getDatabasePath("journey.db")
    return Room.databaseBuilder<JourneyDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
        factory = { JourneyDatabaseConstructor.initialize() }
    )
}
