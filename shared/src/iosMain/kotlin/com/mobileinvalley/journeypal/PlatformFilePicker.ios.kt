package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

actual class PlatformFilePicker : NSObject(), UIDocumentPickerDelegateProtocol {
    private var callback: ((String?) -> Unit)? = null

    actual fun pickJsonFile(onFilePicked: (String?) -> Unit) {
        this.callback = onFilePicked
        
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON), asCopy = true)
        picker.delegate = this
        
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url != null) {
            try {
                val content = NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null)
                callback?.invoke(content)
            } catch (e: Exception) {
                callback?.invoke(null)
            }
        } else {
            callback?.invoke(null)
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        callback?.invoke(null)
    }
}

@Composable
actual fun rememberPlatformFilePicker(): PlatformFilePicker {
    return remember { PlatformFilePicker() }
}
