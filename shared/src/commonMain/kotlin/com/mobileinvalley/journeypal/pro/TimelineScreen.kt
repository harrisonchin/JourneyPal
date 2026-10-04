package com.mobileinvalley.journeypal.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

private val json = Json { prettyPrint = true }

fun formatFormattedTimestamp(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthName = localDateTime.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
    val hour = localDateTime.hour
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val isPm = hour >= 12
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (isPm) "PM" else "AM"
    return "$monthName ${localDateTime.dayOfMonth}, ${localDateTime.year} • $displayHour:$minute $amPm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    dao: JourneyDao? = null,
    onItemClick: (JourneyItem) -> Unit = {}
) {
    val themeModeState = LocalThemeMode.current
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf<Long?>(null) }
    var selectedEndDate by remember { mutableStateOf<Long?>(null) }
    var fullscreenPhotoUri by remember { mutableStateOf<String?>(null) }
    val shareLauncher = rememberPlatformShareLauncher()
    val filePicker = rememberPlatformFilePicker()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val journeyItems by if (dao != null) {
        remember(searchQuery, selectedStartDate, selectedEndDate) {
            val start = selectedStartDate?.let { Instant.fromEpochMilliseconds(it) }
            val end = selectedEndDate?.let { Instant.fromEpochMilliseconds(it + 86399999) } // End of day
            dao.searchAndFilterItems(searchQuery, start, end)
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
                    } else if (start != null) {
                        item.timestamp >= start
                    } else if (end != null) {
                        item.timestamp <= end
                    } else true
                    matchesSearch && matchesDate
                }
            )
        }
    }
    
    val scope = rememberCoroutineScope()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var newNote by rememberSaveable { mutableStateOf("") }
    var latText by rememberSaveable { mutableStateOf("") }
    var lonText by rememberSaveable { mutableStateOf("") }
    var photoUris by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    val imagePicker = rememberImagePickerLauncher { uris ->
        photoUris = uris
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(shadowElevation = 0.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("JourneyPal Pro Timeline") },
                        actions = {
                            IconButton(onClick = {
                                themeModeState.value = when (themeModeState.value) {
                                    ThemeMode.Light -> ThemeMode.Dark
                                    ThemeMode.Dark -> ThemeMode.System
                                    ThemeMode.System -> ThemeMode.Light
                                }
                            }) {
                                val icon = when (themeModeState.value) {
                                    ThemeMode.Light -> Icons.Default.LightMode
                                    ThemeMode.Dark -> Icons.Default.DarkMode
                                    ThemeMode.System -> Icons.Default.SettingsBrightness
                                }
                                Icon(icon, contentDescription = "Toggle Theme")
                            }
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Export Backup (JSON)") },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                val allItems = dao?.getAllItems()?.first() ?: emptyList()
                                                val jsonString = json.encodeToString(allItems)
                                                shareLauncher.shareTextFile("journeypal_backup.json", jsonString)
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Import Backup (JSON)") },
                                        onClick = {
                                            showMenu = false
                                            filePicker.pickJsonFile { content ->
                                                if (content != null) {
                                                    scope.launch {
                                                        try {
                                                            val importedItems = json.decodeFromString<List<JourneyItem>>(content)
                                                            dao?.upsertItems(importedItems)
                                                            snackbarHostState.showSnackbar("Successfully restored ${importedItems.size} entries!")
                                                        } catch (e: Exception) {
                                                            snackbarHostState.showSnackbar("Failed to parse backup file.")
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    )

                    val dateFilterActive = selectedStartDate != null || selectedEndDate != null

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text("Search your journey...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.DateRange,
                                        contentDescription = "Filter by Date",
                                        tint = if (dateFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    if (dateFilterActive) {
                        val startStr = selectedStartDate?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() } ?: "..."
                        val endStr = selectedEndDate?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() } ?: "..."
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = { showDatePicker = true },
                                label = { Text("$startStr - $endStr") },
                                leadingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                trailingIcon = {
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
                            )
                        }
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(journeyItems, key = { it.id }) { item ->
                    JourneyItemRow(
                        item = item,
                        onClick = { onItemClick(item) },
                        onPhotoClick = { uri -> fullscreenPhotoUri = uri }
                    )
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
                                id = "${currentNow.toEpochMilliseconds()}_${Random.nextInt(1000)}",
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

    if (fullscreenPhotoUri != null) {
        FullscreenImageViewer(
            photoUri = fullscreenPhotoUri!!,
            onDismiss = { fullscreenPhotoUri = null }
        )
    }
}

@Composable
fun JourneyItemRow(
    item: JourneyItem,
    onClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.photoUris.isNotEmpty()) {
                    val firstUri = item.photoUris.first()
                    AsyncImage(
                        model = resolveUri(firstUri),
                        contentDescription = "Journey Photo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPhotoClick(firstUri) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.notes,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatFormattedTimestamp(item.timestamp),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1
                    )
                    if (item.latitude != 0.0 || item.longitude != 0.0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Location attached",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            if (item.photoUris.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(item.photoUris.drop(1)) { uri ->
                        AsyncImage(
                            model = resolveUri(uri),
                            contentDescription = "Journey Photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onPhotoClick(uri) },
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
