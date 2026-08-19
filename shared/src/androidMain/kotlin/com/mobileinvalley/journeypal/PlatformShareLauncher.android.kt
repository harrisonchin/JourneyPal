package com.mobileinvalley.journeypal

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class PlatformShareLauncher(private val context: Context) {
    actual fun shareTextFile(fileName: String, content: String) {
        val cacheDir = File(context.cacheDir, "export")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        
        val file = File(cacheDir, fileName)
        file.writeText(content)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export JourneyPal Backup"))
    }
}

@Composable
actual fun rememberPlatformShareLauncher(): PlatformShareLauncher {
    val context = LocalContext.current
    return remember(context) { PlatformShareLauncher(context) }
}
