package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    dao: JourneyDao? = null,
    onItemClick: (JourneyItem) -> Unit = {}
) {
    val journeyItems by if (dao != null) {
        dao.getAllItems().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(getMockJourneyItems()) }
    }
    
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var newNote by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf<List<String>>(emptyList()) }

    val imagePicker = rememberImagePickerLauncher { uris ->
        photoUris = uris
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("JourneyPal Timeline") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Journey Item")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(journeyItems, key = { it.id }) { item ->
                JourneyItemRow(item, onClick = { onItemClick(item) })
            }
        }
    }

    if (showAddDialog) {
        LaunchedEffect(Unit) {
            val location = getCurrentLocation()
            if (location != null) {
                latText = location.latitude.toString()
                lonText = location.longitude.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Journey Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("What's on your mind?")
                    TextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        placeholder = { Text("Enter your notes here...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (photoUris.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(photoUris) { uri ->
                                AsyncImage(
                                    model = resolveUri(uri),
                                    contentDescription = "Selected Photo",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { imagePicker.launch() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (photoUris.isEmpty()) "Add Photos" else "Change Photos")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = latText,
                            onValueChange = { latText = it },
                            label = { Text("Lat") },
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = lonText,
                            onValueChange = { lonText = it },
                            label = { Text("Lon") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            scope.launch {
                                val location = getCurrentLocation()
                                if (location != null) {
                                    latText = location.latitude.toString()
                                    lonText = location.longitude.toString()
                                }
                            }
                        }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Detect Location")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNote.isNotBlank()) {
                            val currentNow = now()
                            
                            val parsedLat = latText.toDoubleOrNull() ?: 0.0
                            val parsedLon = lonText.toDoubleOrNull() ?: 0.0
                            
                            val finalLat = if (parsedLat == 0.0) 37.5483 else parsedLat
                            val finalLon = if (parsedLon == 0.0) -121.9886 else parsedLon
                            
                            val savedPhotoUris = photoUris.toList()

                            val newItem = JourneyItem(
                                id = "${currentNow.toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}",
                                photoUris = savedPhotoUris,
                                timestamp = currentNow,
                                latitude = finalLat,
                                longitude = finalLon,
                                notes = newNote
                            )
                            
                            if (dao != null) {
                                scope.launch {
                                    dao.insertItem(newItem)
                                }
                            }

                            newNote = ""
                            latText = ""
                            lonText = ""
                            photoUris = emptyList()
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    newNote = ""
                    latText = ""
                    lonText = ""
                    photoUris = emptyList()
                    showAddDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JourneyItemRow(
    item: JourneyItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (item.photoUris.isNotEmpty()) {
                    AsyncImage(
                        model = resolveUri(item.photoUris.first()),
                        contentDescription = "Journey Photo",
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.LightGray)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.timestamp.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lat: ${item.latitude}, Lon: ${item.longitude}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (item.photoUris.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(item.photoUris.drop(1)) { uri ->
                        AsyncImage(
                            model = resolveUri(uri),
                            contentDescription = "Journey Photo",
                            modifier = Modifier.size(60.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

fun getMockJourneyItems(): List<JourneyItem> {
    val now = Instant.fromEpochMilliseconds(1715856000000L)
    return listOf(
        JourneyItem(
            id = "1",
            photoUris = emptyList(),
            timestamp = now,
            latitude = 48.8566,
            longitude = 2.3522,
            notes = "Exploring the streets of Paris"
        ),
        JourneyItem(
            id = "2",
            photoUris = emptyList(),
            timestamp = now - 1.days,
            latitude = 52.5200,
            longitude = 13.4050,
            notes = "Enjoying a currywurst in Berlin"
        ),
        JourneyItem(
            id = "3",
            photoUris = emptyList(),
            timestamp = now - 2.days,
            latitude = 41.9028,
            longitude = 12.4964,
            notes = "Visiting the Colosseum in Rome"
        )
    )
}

@Preview
@Composable
fun TimelineScreenPreview() {
    MaterialTheme {
        TimelineScreen()
    }
}
