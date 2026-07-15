package com.mobileinvalley.journeypal

import android.os.Build
import kotlinx.datetime.Instant

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun now(): Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
