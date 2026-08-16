package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyDetailScreen(
    itemId: String,
    dao: JourneyDao?,
    onBack: () -> Unit,
    onShowOnMap: (JourneyItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val itemState = if (dao != null) {
        dao.getItemById(itemId).collectAsState(initial = null)
    } else {
        // Mock fallback if dao is null (e.g. preview)
        remember {
            mutableStateOf(getMockJourneyItems().find { it.id == itemId })
        }
    }
    val journeyItem = itemState.value

    var noteText by remember(journeyItem) { mutableStateOf(journeyItem?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (journeyItem != null && noteText != journeyItem.notes) {
                        IconButton(onClick = {
                            scope.launch {
                                dao?.updateItem(journeyItem.copy(notes = noteText))
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save Changes")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        journeyItem?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image
                if (item.photoUris.isNotEmpty()) {
                    AsyncImage(
                        model = resolveUri(item.photoUris.first()),
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
                    // Editable Notes
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Notes / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Timestamp
                    Text(
                        text = "Recorded on ${item.timestamp}",
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
                            text = "Latitude: ${item.latitude}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Longitude: ${item.longitude}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Button(
                        onClick = { onShowOnMap(item) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Show on Map")
                    }
                    
                    if (noteText != item.notes) {
                        Button(
                            onClick = {
                                scope.launch {
                                    dao?.updateItem(item.copy(notes = noteText))
                                    onBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes")
                        }
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
