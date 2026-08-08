package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.Foundation.*
import platform.PhotosUI.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlinx.cinterop.ExperimentalForeignApi

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<String>) -> Unit): ImagePickerLauncher {
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                
                val results = didFinishPicking.mapNotNull { it as? PHPickerResult }
                if (results.isEmpty()) {
                    onResult(emptyList())
                    return
                }

                val savedUris = mutableListOf<String>()
                var processedCount = 0

                results.forEach { result ->
                    val itemProvider = result.itemProvider
                    if (itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
                        itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
                            dispatch_async(dispatch_get_main_queue()) {
                                if (data is NSData) {
                                    val image = UIImage.imageWithData(data)
                                    if (image != null) {
                                        val jpegData = UIImageJPEGRepresentation(image, 0.8)
                                        if (jpegData != null) {
                                            val fileManager = NSFileManager.defaultManager
                                            val documentsDirectory = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).last() as? NSURL
                                            val fileName = "photo_${NSUUID.UUID().UUIDString}.jpg"
                                            val fileURL = documentsDirectory?.URLByAppendingPathComponent(fileName)
                                            if (fileURL != null) {
                                                jpegData.writeToURL(fileURL, true)
                                                savedUris.add("app-storage://$fileName")
                                            }
                                        }
                                    }
                                }
                                
                                processedCount++
                                if (processedCount == results.size) {
                                    onResult(savedUris)
                                }
                            }
                        }
                    } else {
                        processedCount++
                        if (processedCount == results.size) {
                            onResult(savedUris)
                        }
                    }
                }
            }
        }
    }

    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                val configuration = PHPickerConfiguration()
                configuration.setSelectionLimit(0) // 0 means no limit
                configuration.setFilter(PHPickerFilter.imagesFilter())
                
                val picker = PHPickerViewController(configuration)
                picker.setDelegate(delegate)
                
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                rootViewController?.presentViewController(picker, true, null)
            }
        }
    }
}
