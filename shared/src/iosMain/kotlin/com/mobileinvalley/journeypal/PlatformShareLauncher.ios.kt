package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

actual class PlatformShareLauncher {
    @OptIn(ExperimentalForeignApi::class)
    actual fun shareTextFile(fileName: String, content: String) {
        val fileManager = NSFileManager.defaultManager
        val cacheDirectory = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as? NSURL
        val fileURL = cacheDirectory?.URLByAppendingPathComponent(fileName)
        
        if (fileURL != null) {
            val nsString = content as NSString
            nsString.writeToURL(fileURL, true, NSUTF8StringEncoding, null)
            
            val activityController = UIActivityViewController(listOf(fileURL), null)
            
            val window = UIApplication.sharedApplication.keyWindow
            val rootViewController = window?.rootViewController
            
            // For iPad compatibility
            activityController.popoverPresentationController()?.sourceView = rootViewController?.view
            
            rootViewController?.presentViewController(activityController, animated = true, completion = null)
        }
    }
}

@Composable
actual fun rememberPlatformShareLauncher(): PlatformShareLauncher {
    return remember { PlatformShareLauncher() }
}
