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
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var newNoteSubject by remember { mutableStateOf("Physics") }

    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editSubject by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val filteredNotes = remember(allNotes, searchQuery) {
        if (searchQuery.isBlank()) allNotes
        else allNotes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.contentMarkdown.contains(searchQuery, ignoreCase = true) ||
            it.subjectName.contains(searchQuery, ignoreCase = true)
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
            if (filteredNotes.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isBlank()) "No notes created yet. Tap '+' to write your first study note!" else "No notes match \"$searchQuery\".",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                items(filteredNotes) { note ->
                    val isBookmarked = remember(bookmarks, note.id) {
                        bookmarks.any { it.itemType.equals("NOTE", ignoreCase = true) && it.itemId == note.id }
                    }

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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                type = "NOTE",
                                                itemId = note.id,
                                                title = note.title,
                                                subtitle = note.subjectName,
                                                route = "notes"
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            editingNote = note
                                            editTitle = note.title
                                            editContent = note.contentMarkdown
                                            editSubject = note.subjectName
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Note",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
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
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
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
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.selectedBot.value = AiBotType.AI_NOTES_ASSISTANT
                                        viewModel.aiChatInput.value = "Summarize and extract core formulas from this note:\n\n${note.contentMarkdown}"
                                        viewModel.navigateTo(StudyForgeRoute.AiBots)
                                    }
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Summarize", fontSize = 11.sp)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.selectedBot.value = AiBotType.AI_TUTOR
                                        viewModel.aiChatInput.value = "Explain in depth the concepts in this study note titled '${note.title}':\n\n${note.contentMarkdown}"
                                        viewModel.navigateTo(StudyForgeRoute.AiBots)
                                    }
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Explain", fontSize = 11.sp)
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
                                    Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Card", fontSize = 11.sp)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.selectedBot.value = AiBotType.AI_QUESTION_GENERATOR
                                        viewModel.aiChatInput.value = "Generate 3 practice questions with detailed solutions based on this note:\n\n${note.contentMarkdown}"
                                        viewModel.navigateTo(StudyForgeRoute.AiBots)
                                    }
                                ) {
                                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Questions", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Note Dialog
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
                        placeholder = { Text("e.g. Physics, Math, Chemistry") },
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
                                        subjectName = newNoteSubject.trim().ifBlank { "General" },
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

    // Edit Note Dialog
    if (editingNote != null) {
        AlertDialog(
            onDismissRequest = { editingNote = null },
            title = { Text("Edit Study Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingNote?.let { n ->
                            viewModel.editNote(
                                n.copy(
                                    title = editTitle.trim(),
                                    subjectName = editSubject.trim(),
                                    contentMarkdown = editContent.trim()
                                )
                            )
                        }
                        editingNote = null
                    },
                    enabled = editTitle.isNotBlank()
                ) {
                    Text("Update Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNote = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
