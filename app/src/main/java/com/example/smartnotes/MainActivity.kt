package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartnotes.ui.theme.SmartNotesTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartNotesTheme {
                val viewModel: NoteViewModel = viewModel()
                val notes by viewModel.allNotes.collectAsState(initial = emptyList())
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { CenterAlignedTopAppBar(title = { Text("Smart AI Notes") }) }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                        NoteInput(onAddNote = { title, content ->
                            viewModel.addNote(title, content)
                        })
                        Spacer(modifier = Modifier.height(16.dp))
                        NoteList(notes, onDeleteNote = { viewModel.deleteNote(it) })
                    }
                }
            }
        }
    }
}

@Composable
fun NoteInput(onAddNote: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Column {
        TextField(
            value = title, 
            onValueChange = { title = it }, 
            label = { Text("Title") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = content, 
            onValueChange = { content = it }, 
            label = { Text("Content") }, 
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Button(
            onClick = { 
                if (title.isNotBlank() && content.isNotBlank()) {
                    onAddNote(title, content)
                    title = ""
                    content = ""
                }
            }, 
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
        ) {
            Text("Add Note (AI will tag)")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteList(notes: List<Note>, onDeleteNote: (Note) -> Unit) {
    LazyColumn {
        items(notes, key = { it.id }) { note ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteNote(note)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                            else -> Color.Red
                        }, label = "color"
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)) // Matching Card corner radius
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                            Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            SuggestionChip(
                                onClick = {}, 
                                label = { Text("Mood: ${note.mood}") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SuggestionChip(
                                onClick = {}, 
                                label = { Text("Tags: ${note.tags}") }
                            )
                        }
                    }
                }
            }
        }
    }
}
