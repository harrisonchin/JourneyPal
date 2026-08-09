package com.mobileinvalley.journeypal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun JourneyMapView(
    items: List<JourneyItem>,
    modifier: Modifier = Modifier,
    onItemClick: (JourneyItem) -> Unit = {}
)
