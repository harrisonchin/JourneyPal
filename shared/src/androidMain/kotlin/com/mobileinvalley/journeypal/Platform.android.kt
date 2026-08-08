package com.mobileinvalley.journeypal

import android.net.Uri
import android.os.Build
import kotlinx.datetime.Instant

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun now(): Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())

actual fun resolveUri(uri: String?): Any? {
    if (uri == null) return null
    return try {
        if (uri.startsWith("app-storage://")) {
            val fileName = uri.substringAfter("app-storage://")
            val file = java.io.File(appContext.filesDir, fileName)
            Uri.fromFile(file)
        } else if (uri.startsWith("content://") || uri.startsWith("file://")) {
            Uri.parse(uri)
        } else if (uri.startsWith("/")) {
            Uri.fromFile(java.io.File(uri))
        } else {
            uri
        }
    } catch (e: Exception) {
        uri
    }
}
