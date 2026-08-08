package com.mobileinvalley.journeypal

import platform.UIKit.UIDevice
import platform.Foundation.*
import kotlinx.datetime.Instant

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun now(): Instant = Instant.fromEpochMilliseconds((NSDate().timeIntervalSince1970 * 1000).toLong())

actual fun resolveUri(uri: String?): Any? {
    if (uri == null) return null
    if (uri.startsWith("app-storage://")) {
        val fileName = uri.substringAfter("app-storage://")
        val fileManager = NSFileManager.defaultManager
        val documentsDirectory = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).last() as? NSURL
        val fileURL = documentsDirectory?.URLByAppendingPathComponent(fileName)
        return fileURL?.absoluteString
    }
    return uri
}
