package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.StudyDocumentEntity
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

@Composable
fun DocumentReaderScreen(viewModel: StudyForgeViewModel) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val selectedDoc by viewModel.selectedDocument.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    val currentDoc = selectedDoc ?: documents.firstOrNull()
    var showAddDialog by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    var newDocTitle by remember { mutableStateOf("") }
    var newDocSummary by remember { mutableStateOf("") }
    var newDocContent by remember { mutableStateOf("") }
    var newDocPages by remember { mutableStateOf("5") }

    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember(currentDoc?.id) { mutableIntStateOf(1) }

    // File Picker Launcher for Android Device File Import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                var displayName = "Imported Document"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIdx)
                    }
                }

                // Read text stream safely if possible
                var contentText = ""
                try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                        contentText = reader.readText()
                    }
                } catch (_: Exception) {
                    contentText = ""
                }

                val ext = displayName.substringAfterLast('.', "PDF").uppercase()
                val estimatedPages = (contentText.length / 1500).coerceAtLeast(1)
                val summary = if (contentText.isNotBlank()) {
                    contentText.take(200).replace("\n", " ") + "..."
                } else {
                    "Document imported from device ($displayName)."
                }

                viewModel.addDocument(
                    title = displayName,
                    summary = summary,
                    content = if (contentText.isNotBlank()) contentText else "Full binary document contents for $displayName. Use AI specialist tools below to extract notes, flashcards, and exam questions.",
                    pageCount = estimatedPages,
                    fileType = ext
                )
                importStatusMessage = "Successfully imported '$displayName'!"
            } catch (e: Exception) {
                importStatusMessage = "Import failed: ${e.localizedMessage}"
            }
        }
    }

    val isBookmarked = remember(bookmarks, currentDoc) {
        if (currentDoc == null) false
        else bookmarks.any { it.itemType.equals("DOCUMENT", ignoreCase = true) && it.itemId == currentDoc.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("document_reader_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Document Selector Carousel Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeCyan.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Study Material & Documents",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${documents.size} Document(s) in Library",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import File", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Text", fontSize = 12.sp)
                            }
                        }
                    }

                    if (importStatusMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForgeEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = importStatusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeEmerald,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (documents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        documents.forEach { doc ->
                            val isSelected = currentDoc?.id == doc.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.selectedDocument.value = doc
                                        currentPage = 1
                                    },
                                color = if (isSelected) ForgeCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ForgeCyan) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ForgeCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${doc.pageCount}p",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No documents imported yet. Tap 'Import File' to load PDFs/notes from device or '+ Add Text'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Document Reader View
        item {
            if (currentDoc != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                    text = "${currentDoc.fileType} • Page $currentPage of ${currentDoc.pageCount.coerceAtLeast(1)}",
                                    color = ForgeIndigoLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.toggleBookmark(
                                            type = "DOCUMENT",
                                            itemId = currentDoc.id,
                                            title = currentDoc.title,
                                            subtitle = "${currentDoc.fileType} • ${currentDoc.pageCount} Pages",
                                            route = "document_reader"
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark Document",
                                        tint = if (isBookmarked) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.deleteDocument(currentDoc.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Document",
                                        tint = ForgeRose
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentDoc.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Overview: ${currentDoc.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Search within document
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search inside document text...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Text content viewport
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 320.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            val textToShow = if (searchQuery.isNotBlank()) {
                                currentDoc.contentExtract.lines().filter { it.contains(searchQuery, ignoreCase = true) }.joinToString("\n")
                            } else {
                                currentDoc.contentExtract
                            }

                            Text(
                                text = if (textToShow.isNotBlank()) textToShow else "No text match found.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Page Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentPage > 1) currentPage-- },
                                enabled = currentPage > 1
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Previous Page")
                            }

                            Text(
                                text = "Page $currentPage / ${currentDoc.pageCount.coerceAtLeast(1)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = { if (currentPage < currentDoc.pageCount) currentPage++ },
                                enabled = currentPage < currentDoc.pageCount
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next Page")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // AI Tools for Current Document
                        Text(
                            text = "AI Document Engines",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.selectedBot.value = AiBotType.AI_TUTOR
                                    viewModel.aiChatInput.value = "Analyze and summarize this study material:\n\n${currentDoc.title}\n${currentDoc.contentExtract.take(800)}"
                                    viewModel.navigateTo(StudyForgeRoute.AiBots)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Summarize", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.selectedBot.value = AiBotType.AI_FLASHCARD_GENERATOR
                                    viewModel.aiChatInput.value = "Create high-yield active recall flashcards from this text:\n\n${currentDoc.contentExtract.take(800)}"
                                    viewModel.navigateTo(StudyForgeRoute.AiBots)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Make Cards", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.selectedBot.value = AiBotType.AI_QUESTION_GENERATOR
                                    viewModel.aiChatInput.value = "Generate practice questions with solutions based on this excerpt:\n\n${currentDoc.contentExtract.take(800)}"
                                    viewModel.navigateTo(StudyForgeRoute.AiBots)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Make Test", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Study Material") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g. Chapter 4 - Electrostatics Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocSummary,
                        onValueChange = { newDocSummary = it },
                        label = { Text("Brief Summary") },
                        placeholder = { Text("e.g. Coulomb's Law, Electric Fields & Gauss Law") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocPages,
                        onValueChange = { newDocPages = it },
                        label = { Text("Page Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDocContent,
                        onValueChange = { newDocContent = it },
                        label = { Text("Document Content / Excerpt") },
                        placeholder = { Text("Paste chapter notes, lecture notes, or key definitions...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pages = newDocPages.toIntOrNull() ?: 1
                        if (newDocTitle.isNotBlank()) {
                            viewModel.addDocument(
                                title = newDocTitle,
                                summary = newDocSummary.ifBlank { "Imported Study Material" },
                                content = newDocContent.ifBlank { "Full text reference for ${newDocTitle}." },
                                pageCount = pages,
                                fileType = "PDF"
                            )
                            showAddDialog = false
                            newDocTitle = ""
                            newDocSummary = ""
                            newDocContent = ""
                        }
                    },
                    enabled = newDocTitle.isNotBlank()
                ) {
                    Text("Save Document")
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
