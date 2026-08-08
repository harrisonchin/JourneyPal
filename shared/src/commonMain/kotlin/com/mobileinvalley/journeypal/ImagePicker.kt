package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onResult: (List<String>) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
