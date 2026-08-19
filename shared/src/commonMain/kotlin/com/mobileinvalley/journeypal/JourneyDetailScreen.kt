package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
    var selectedLat by remember(journeyItem) { mutableDoubleStateOf(journeyItem?.latitude ?: 0.0) }
    var selectedLon by remember(journeyItem) { mutableDoubleStateOf(journeyItem?.longitude ?: 0.0) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullscreenPhotoUri by remember { mutableStateOf<String?>(null) }

    val hasChanges = journeyItem != null && (noteText != journeyItem.notes || selectedLat != journeyItem.latitude || selectedLon != journeyItem.longitude)

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
                    if (journeyItem != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                        }
                    }
                    if (journeyItem != null && hasChanges) {
                        IconButton(onClick = {
                            scope.launch {
                                dao?.updateItem(journeyItem.copy(
                                    notes = noteText,
                                    latitude = selectedLat,
                                    longitude = selectedLon
                                ))
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
                    val heroUri = item.photoUris.first()
                    AsyncImage(
                        model = resolveUri(heroUri),
                        contentDescription = "Hero Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color.LightGray)
                            .clickable { fullscreenPhotoUri = heroUri },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showLocationPicker = true }) {
                                Icon(Icons.Default.Map, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Pick on Map")
                            }
                        }
                        Text(
                            text = "Latitude: $selectedLat",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Longitude: $selectedLon",
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
                    
                    if (hasChanges) {
                        Button(
                            onClick = {
                                scope.launch {
                                    dao?.updateItem(item.copy(
                                        notes = noteText,
                                        latitude = selectedLat,
                                        longitude = selectedLon
                                    ))
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

    if (showLocationPicker && journeyItem != null) {
        var tempLat by remember { mutableDoubleStateOf(selectedLat) }
        var tempLon by remember { mutableDoubleStateOf(selectedLon) }

        AlertDialog(
            onDismissRequest = { showLocationPicker = false },
            title = { Text("Select Location") },
            text = {
                Column(modifier = Modifier.height(400.dp)) {
                    Text("Tap on the map to select a new location.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    LocationPickerMapView(
                        initialLatitude = selectedLat,
                        initialLongitude = selectedLon,
                        modifier = Modifier.fillMaxSize().weight(1f),
                        onLocationSelected = { lat, lon ->
                            tempLat = lat
                            tempLon = lon
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedLat = tempLat
                    selectedLon = tempLon
                    showLocationPicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog && journeyItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this journey item? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dao?.deleteItem(journeyItem)
                            showDeleteDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (fullscreenPhotoUri != null) {
        FullscreenImageViewer(
            photoUri = fullscreenPhotoUri!!,
            onDismiss = { fullscreenPhotoUri = null }
        )
    }
}
