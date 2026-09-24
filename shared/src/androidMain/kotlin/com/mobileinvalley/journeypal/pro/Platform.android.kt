package com.mobileinvalley.journeypal.pro

import android.net.Uri
import android.os.Build
import kotlinx.datetime.Instant
import java.io.File

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
            if (isAppContextInitialized()) {
                val file = File(appContext.filesDir, fileName)
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    null
                }
            } else {
                null
            }
        } else if (uri.startsWith("content://") || uri.startsWith("file://")) {
            Uri.parse(uri)
        } else if (uri.startsWith("/")) {
            val file = File(uri)
            if (file.exists()) Uri.fromFile(file) else null
        } else {
            uri
        }
    } catch (e: Exception) {
        null
    }
}
