package com.mobileinvalley.journeypal.pro

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { 
    val database = remember { provideDatabase() }
    App(database) 
}