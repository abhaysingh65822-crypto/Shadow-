package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.FlashcardEntity
import com.example.data.NoteEntity
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(viewModel: StudyForgeViewModel) {
    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var newNoteSubject by remember { mutableStateOf("Physics") }
    var isAiAssisting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val filteredNotes = remember(allNotes, searchQuery) {
        if (searchQuery.isBlank()) allNotes
        else allNotes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.contentMarkdown.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_screen")
    ) {
        // Search & Add Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search concepts & notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            FloatingActionButton(
                onClick = { showCreateNoteDialog = true },
                containerColor = ForgeIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Note")
            }
        }

        // Notes List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredNotes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ForgeIndigoLight.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = note.subjectName,
                                    color = ForgeIndigoLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.repository.updateNote(
                                                note.copy(isFavorite = !note.isFavorite)
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = if (note.isFavorite) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.repository.deleteNote(note.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.contentMarkdown,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 6,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // AI Actions Bar for Note
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.selectedBot.value = AiBotType.AI_NOTES_ASSISTANT
                                    viewModel.aiChatInput.value = "Summarize and extract core formulas from this note:\n\n${note.contentMarkdown}"
                                    viewModel.navigateTo(StudyForgeRoute.AiBots)
                                }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Summarize", fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.repository.insertFlashcard(
                                            FlashcardEntity(
                                                front = "Key takeaways from: ${note.title}",
                                                back = note.contentMarkdown.take(150),
                                                cardType = "STANDARD"
                                            )
                                        )
                                        viewModel.navigateTo(StudyForgeRoute.Flashcards)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Make Flashcard", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateNoteDialog) {
        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false },
            title = { Text("New Study Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. Work-Energy Core Laws") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNoteSubject,
                        onValueChange = { newNoteSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Content (Markdown / Notes)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.repository.insertNote(
                                    NoteEntity(
                                        title = newNoteTitle.trim(),
                                        contentMarkdown = newNoteContent.trim(),
                                        subjectName = newNoteSubject.trim(),
                                        folder = "General"
                                    )
                                )
                                showCreateNoteDialog = false
                                newNoteTitle = ""
                                newNoteContent = ""
                            }
                        }
                    }
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
