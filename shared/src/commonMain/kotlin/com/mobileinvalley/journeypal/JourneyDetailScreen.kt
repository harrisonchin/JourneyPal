package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyDetailScreen(
    itemId: String,
    dao: JourneyDao?,
    onBack: () -> Unit,
    onShowOnMap: (JourneyItem) -> Unit
) {
    val item by if (dao != null) {
        dao.getItemById(itemId).collectAsState(initial = null)
    } else {
        // Mock fallback if dao is null (e.g. preview)
        androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf(getMockJourneyItems().find { it.id == itemId })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        item?.let { journeyItem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image
                if (journeyItem.photoUris.isNotEmpty()) {
                    AsyncImage(
                        model = resolveUri(journeyItem.photoUris.first()),
                        contentDescription = "Hero Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title / Notes
                    Text(
                        text = journeyItem.notes,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Timestamp
                    Text(
                        text = "Recorded on ${journeyItem.timestamp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    HorizontalDivider()

                    // Location Details
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Latitude: ${journeyItem.latitude}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Longitude: ${journeyItem.longitude}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Button(
                        onClick = { onShowOnMap(journeyItem) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Show on Map")
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
