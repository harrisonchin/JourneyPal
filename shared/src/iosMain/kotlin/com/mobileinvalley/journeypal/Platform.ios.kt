package com.mobileinvalley.journeypal

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlinx.datetime.Instant

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun now(): Instant = Instant.fromEpochMilliseconds((NSDate().timeIntervalSince1970 * 1000).toLong())
