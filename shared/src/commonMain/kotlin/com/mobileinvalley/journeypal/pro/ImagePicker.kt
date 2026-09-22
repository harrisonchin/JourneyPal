package com.mobileinvalley.journeypal.pro

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onResult: (List<String>) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
