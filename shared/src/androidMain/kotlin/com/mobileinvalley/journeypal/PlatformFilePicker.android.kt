package com.mobileinvalley.journeypal

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

actual class PlatformFilePicker(
    private val launchPicker: ((String?) -> Unit) -> Unit
) {
    actual fun pickJsonFile(onFilePicked: (String?) -> Unit) {
        launchPicker(onFilePicked)
    }
}

@Composable
actual fun rememberPlatformFilePicker(): PlatformFilePicker {
    val context = LocalContext.current
    var callback by remember { mutableStateOf<((String?) -> Unit)?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { 
                    it.bufferedReader().readText()
                }
                callback?.invoke(content)
            } catch (e: Exception) {
                callback?.invoke(null)
            }
        } else {
            callback?.invoke(null)
        }
    }
    
    return remember(launcher) {
        PlatformFilePicker { cb ->
            callback = cb
            launcher.launch("application/json")
        }
    }
}
