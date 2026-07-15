package com.mobileinvalley.journeypal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(dao: JourneyDao? = null) {
    // 2. Observe the live Flow from the DAO using collectAsState
    val journeyItems by if (dao != null) {
        dao.getAllItems().collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(getMockJourneyItems()) }
    }
    
    // 3. Create a CoroutineScope to launch asynchronous database operations
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var newNote by remember { mutableStateOf("") }

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
                JourneyItemRow(item)
            }
        }
    }

    if (showAddDialog) {
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNote.isNotBlank()) {
                            val currentNow = now()
                            val newItem = JourneyItem(
                                id = "${currentNow.toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}",
                                photoPath = "",
                                timestamp = currentNow,
                                latitude = 0.0,
                                longitude = 0.0,
                                notes = newNote
                            )
                            
                            // 3. Call dao.insertItem(newItem) asynchronously inside the scope
                            if (dao != null) {
                                scope.launch {
                                    dao.insertItem(newItem)
                                }
                            }

                            newNote = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JourneyItemRow(item: JourneyItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Placeholder for Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.timestamp.toString(), // Simple string representation
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
    }
}

fun getMockJourneyItems(): List<JourneyItem> {
    val now = Instant.fromEpochMilliseconds(1715856000000L) // Example fixed time
    return listOf(
        JourneyItem(
            id = "1",
            photoPath = "path/to/photo1.jpg",
            timestamp = now,
            latitude = 48.8566,
            longitude = 2.3522,
            notes = "Exploring the streets of Paris"
        ),
        JourneyItem(
            id = "2",
            photoPath = "path/to/photo2.jpg",
            timestamp = now - 1.days,
            latitude = 52.5200,
            longitude = 13.4050,
            notes = "Enjoying a currywurst in Berlin"
        ),
        JourneyItem(
            id = "3",
            photoPath = "path/to/photo3.jpg",
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
