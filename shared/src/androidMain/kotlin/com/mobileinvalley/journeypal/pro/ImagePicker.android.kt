package com.mobileinvalley.journeypal.pro

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.util.UUID

@Composable
actual fun rememberImagePickerLauncher(onResult: (List<String>) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) {
            onResult(emptyList())
            return@rememberLauncherForActivityResult
        }
        val savedPaths = uris.mapNotNull { uri ->
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // Ignored if URI permissions are already handled or not persistable
                }

                context.contentResolver.openInputStream(uri)?.use { input ->
                    val fileName = "photo_${UUID.randomUUID()}.jpg"
                    val file = File(context.filesDir, fileName)
                    file.outputStream().use { output ->
                        input.copyTo(output)
                        output.flush()
                    }
                    if (file.exists() && file.length() > 0) {
                        "app-storage://$fileName"
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        onResult(savedPaths)
    }
    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }
    }
}
