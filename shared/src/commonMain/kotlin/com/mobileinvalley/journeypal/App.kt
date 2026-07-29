package com.mobileinvalley.journeypal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun App(database: JourneyDatabase? = null) {
    val dao = database?.journeyDao()
    val journeyItems by if (dao != null) {
        dao.getAllItems().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(getMockJourneyItems()) }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Timeline") },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Timeline") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Map") },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> TimelineScreen(dao)
                    1 -> JourneyMapView(journeyItems, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
