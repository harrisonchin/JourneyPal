package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable

expect class PlatformShareLauncher {
    fun shareTextFile(fileName: String, content: String)
}

@Composable
expect fun rememberPlatformShareLauncher(): PlatformShareLauncher
