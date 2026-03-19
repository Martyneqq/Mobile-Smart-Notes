package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                        NoteList(notes)
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

@Composable
fun NoteList(notes: List<Note>) {
    LazyColumn {
        items(notes) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
