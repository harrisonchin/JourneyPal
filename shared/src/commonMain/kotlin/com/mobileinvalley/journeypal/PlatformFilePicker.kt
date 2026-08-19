package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable

expect class PlatformFilePicker {
    fun pickJsonFile(onFilePicked: (String?) -> Unit)
}

@Composable
expect fun rememberPlatformFilePicker(): PlatformFilePicker
