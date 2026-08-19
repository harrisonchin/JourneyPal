package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    dao: JourneyDao? = null,
    onItemClick: (JourneyItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }
    
    val journeyItems by if (dao != null) {
        remember(searchQuery, selectedStartDate, selectedEndDate) {
            val start = selectedStartDate?.let { Instant.fromEpochMilliseconds(it) }
            val end = selectedEndDate?.let { Instant.fromEpochMilliseconds(it + 86399999) } // End of day

            if (start != null && end != null) {
                dao.searchAndFilter(searchQuery, start, end)
            } else if (searchQuery.isNotEmpty()) {
                dao.searchItems(searchQuery)
            } else {
                dao.getAllItems()
            }
        }.collectAsState(initial = emptyList())
    } else {
        remember(searchQuery, selectedStartDate, selectedEndDate) {
            val start = selectedStartDate?.let { Instant.fromEpochMilliseconds(it) }
            val end = selectedEndDate?.let { Instant.fromEpochMilliseconds(it + 86399999) }
            
            mutableStateOf(
                getMockJourneyItems().filter { item ->
                    val matchesSearch = item.notes.contains(searchQuery, ignoreCase = true)
                    val matchesDate = if (start != null && end != null) {
                        item.timestamp in start..end
                    } else true
                    matchesSearch && matchesDate
                }
            )
        }
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
            Surface(shadowElevation = 4.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("JourneyPal Timeline") }
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search your journey...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dateFilterActive = selectedStartDate != null && selectedEndDate != null
                        FilterChip(
                            selected = dateFilterActive,
                            onClick = { showDatePicker = true },
                            label = {
                                if (dateFilterActive) {
                                    val startStr = Instant.fromEpochMilliseconds(selectedStartDate!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    val endStr = Instant.fromEpochMilliseconds(selectedEndDate!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    Text("$startStr - $endStr")
                                } else {
                                    Text("Filter by Date")
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                            trailingIcon = if (dateFilterActive) {
                                {
                                    IconButton(
                                        onClick = {
                                            selectedStartDate = null
                                            selectedEndDate = null
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear date filter")
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Journey Item")
            }
        }
    ) { paddingValues ->
        if (journeyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "No journey entries yet." else "No journey entries match your search.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
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

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedStartDate = dateRangePickerState.selectedStartDateMillis
                        selectedEndDate = dateRangePickerState.selectedEndDateMillis
                        showDatePicker = false
                    },
                    enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth().height(500.dp)
            )
        }
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
