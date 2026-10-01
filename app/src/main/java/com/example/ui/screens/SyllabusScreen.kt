package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.ChapterEntity
import com.example.data.SubjectEntity
import com.example.data.TopicEntity
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SyllabusScreen(viewModel: StudyForgeViewModel) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allChapters by viewModel.chapters.collectAsStateWithLifecycle()
    val allTopics by viewModel.topics.collectAsStateWithLifecycle()

    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }
    var expandedChapterId by remember { mutableStateOf<Long?>(null) }

    // Dialog States
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showEditSubjectDialog by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAiSyllabusDialog by remember { mutableStateOf(false) }
    var chapterToEdit by remember { mutableStateOf<ChapterEntity?>(null) }
    var chapterToDelete by remember { mutableStateOf<ChapterEntity?>(null) }
    var topicToEdit by remember { mutableStateOf<TopicEntity?>(null) }
    var topicToDelete by remember { mutableStateOf<TopicEntity?>(null) }

    // Form fields
    var subjectName by remember { mutableStateOf("") }
    var subjectCode by remember { mutableStateOf("") }
    var chapterTitle by remember { mutableStateOf("") }
    var topicTitle by remember { mutableStateOf("") }
    var topicDifficulty by remember { mutableStateOf("MEDIUM") }
    var aiSubjectName by remember { mutableStateOf("") }
    var aiExamTarget by remember { mutableStateOf("High School / Engineering Exam") }
    var isAiGenerating by remember { mutableStateOf(false) }
    var aiMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Ensure selectedSubjectId points to a valid subject if available
    LaunchedEffect(subjects) {
        if (subjects.isNotEmpty() && subjects.none { it.id == selectedSubjectId }) {
            selectedSubjectId = subjects.first().id
        }
    }

    val currentSubject = remember(subjects, selectedSubjectId) {
        subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    }

    val currentSubjectChapters = remember(allChapters, selectedSubjectId) {
        allChapters.filter { it.subjectId == selectedSubjectId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("syllabus_screen")
    ) {
        if (subjects.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = ForgeIndigoLight,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Academic Syllabus Added",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create your custom subjects manually or use the AI Syllabus Builder to generate a complete curriculum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showAddSubjectDialog = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Subject")
                            }
                            Button(
                                onClick = { showAiSyllabusDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Builder")
                            }
                        }
                    }
                }
            }
        } else {
            // Subject Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = subjects.indexOfFirst { it.id == selectedSubjectId }.coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                subjects.forEach { subject ->
                    Tab(
                        selected = subject.id == selectedSubjectId,
                        onClick = {
                            selectedSubjectId = subject.id
                            expandedChapterId = null
                        },
                        text = {
                            Text(
                                text = subject.name,
                                fontWeight = if (subject.id == selectedSubjectId) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
                // Add Subject Tab Action
                Tab(
                    selected = false,
                    onClick = {
                        subjectName = ""
                        subjectCode = ""
                        showAddSubjectDialog = true
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontWeight = FontWeight.Bold, color = ForgeIndigoLight)
                        }
                    }
                )
            }

            // Top Action Bar for active subject
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = {
                                chapterTitle = ""
                                showAddChapterDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Chapter", fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                aiSubjectName = currentSubject?.name ?: ""
                                showAiSyllabusDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForgeIndigoLight)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Syllabus", fontSize = 12.sp, color = ForgeIndigoLight)
                        }
                    }

                    // Manage Subject menu
                    Row {
                        IconButton(
                            onClick = {
                                currentSubject?.let {
                                    subjectName = it.name
                                    subjectCode = it.code
                                    showEditSubjectDialog = true
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Subject", modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { showDeleteSubjectDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Subject", tint = ForgeRose, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Chapters & Topics List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentSubject?.name ?: "Syllabus"} (${currentSubjectChapters.size} Chapters)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap chapter to expand",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (currentSubjectChapters.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No chapters in this subject yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        chapterTitle = ""
                                        showAddChapterDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add First Chapter")
                                }
                            }
                        }
                    }
                }

                items(currentSubjectChapters) { chapter ->
                    val isExpanded = expandedChapterId == chapter.id
                    val chapterTopics = remember(allTopics, chapter.id) {
                        allTopics.filter { it.chapterId == chapter.id }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chapter_card_${chapter.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isExpanded) ForgeIndigoLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedChapterId = if (isExpanded) null else chapter.id
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Chapter ${chapter.orderIndex}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ForgeIndigoLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            chapterToEdit = chapter
                                            chapterTitle = chapter.title
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Chapter", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { chapterToDelete = chapter },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Chapter", tint = ForgeRose, modifier = Modifier.size(16.dp))
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Expand",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Expanded Topics Section
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Topics (${chapterTopics.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        TextButton(
                                            onClick = {
                                                topicTitle = ""
                                                topicDifficulty = "MEDIUM"
                                                showAddTopicDialog = true
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Add Topic", fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (chapterTopics.isEmpty()) {
                                        Text(
                                            text = "No topics added yet. Tap 'Add Topic' above.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        chapterTopics.forEach { topic ->
                                            TopicItemRow(
                                                topic = topic,
                                                onToggleComplete = { viewModel.toggleTopicCompleted(topic) },
                                                onEdit = {
                                                    topicToEdit = topic
                                                    topicTitle = topic.title
                                                    topicDifficulty = topic.difficultyLevel
                                                },
                                                onDelete = { topicToDelete = topic },
                                                onAskAi = {
                                                    viewModel.selectedBot.value = AiBotType.AI_TUTOR
                                                    viewModel.aiChatInput.value = "Explain ${topic.title} in ${chapter.title} step-by-step."
                                                    viewModel.navigateTo(StudyForgeRoute.AiBots)
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Chapter Action Shortcuts
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.setPomodoroSubject(chapter.subjectId, currentSubject?.name ?: "Subject")
                                                viewModel.navigateTo(StudyForgeRoute.Pomodoro)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Focus", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.selectedBot.value = AiBotType.AI_TUTOR
                                                viewModel.aiChatInput.value = "Give me an intuitive breakdown and formula summary for ${chapter.title}."
                                                viewModel.navigateTo(StudyForgeRoute.AiBots)
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Ask AI", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // Dialogs: Subject Add / Edit / Delete
    // ==========================================
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add New Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name (e.g. Biology, History)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code (e.g. BIO-101)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.addSubject(subjectName, subjectCode, "#4F46E5", "menu_book")
                            showAddSubjectDialog = false
                        }
                    }
                ) {
                    Text("Save Subject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditSubjectDialog && currentSubject != null) {
        AlertDialog(
            onDismissRequest = { showEditSubjectDialog = false },
            title = { Text("Edit Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            viewModel.editSubject(currentSubject.copy(name = subjectName.trim(), code = subjectCode.trim()))
                            showEditSubjectDialog = false
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteSubjectDialog && currentSubject != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text("Delete Subject?") },
            text = {
                Text("This will permanently remove \"${currentSubject.name}\" along with all its chapters, topics, and assigned questions.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(currentSubject.id)
                        showDeleteSubjectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeRose)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ==========================================
    // Dialogs: Chapter Add / Edit / Delete
    // ==========================================
    if (showAddChapterDialog) {
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("Add Chapter") },
            text = {
                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Chapter Title (e.g. Thermodynamics)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitle.isNotBlank()) {
                            viewModel.addChapter(selectedSubjectId, chapterTitle)
                            showAddChapterDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (chapterToEdit != null) {
        AlertDialog(
            onDismissRequest = { chapterToEdit = null },
            title = { Text("Edit Chapter") },
            text = {
                OutlinedTextField(
                    value = chapterTitle,
                    onValueChange = { chapterTitle = it },
                    label = { Text("Chapter Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitle.isNotBlank()) {
                            viewModel.editChapter(chapterToEdit!!.copy(title = chapterTitle.trim()))
                            chapterToEdit = null
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (chapterToDelete != null) {
        AlertDialog(
            onDismissRequest = { chapterToDelete = null },
            title = { Text("Delete Chapter?") },
            text = { Text("Delete \"${chapterToDelete!!.title}\" and all associated topics?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChapter(chapterToDelete!!.id)
                        chapterToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeRose)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // ==========================================
    // Dialogs: Topic Add / Edit / Delete
    // ==========================================
    if (showAddTopicDialog && expandedChapterId != null) {
        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            FilterChip(
                                selected = topicDifficulty == diff,
                                onClick = { topicDifficulty = diff },
                                label = { Text(diff, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicTitle.isNotBlank()) {
                            viewModel.addTopic(expandedChapterId!!, topicTitle, topicDifficulty)
                            showAddTopicDialog = false
                        }
                    }
                ) {
                    Text("Save Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (topicToEdit != null) {
        AlertDialog(
            onDismissRequest = { topicToEdit = null },
            title = { Text("Edit Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topicTitle,
                        onValueChange = { topicTitle = it },
                        label = { Text("Topic Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            FilterChip(
                                selected = topicDifficulty == diff,
                                onClick = { topicDifficulty = diff },
                                label = { Text(diff, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicTitle.isNotBlank()) {
                            viewModel.editTopic(topicToEdit!!.copy(title = topicTitle.trim(), difficultyLevel = topicDifficulty))
                            topicToEdit = null
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (topicToDelete != null) {
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = { Text("Delete Topic?") },
            text = { Text("Are you sure you want to remove \"${topicToDelete!!.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTopic(topicToDelete!!.id)
                        topicToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeRose)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // ==========================================
    // Dialog: Create Syllabus with AI (Section 14)
    // ==========================================
    if (showAiSyllabusDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAiGenerating) showAiSyllabusDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForgeIndigoLight)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Syllabus Generator")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a subject or exam syllabus topic. AI will automatically structure chapters, topics, and subtopics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = aiSubjectName,
                        onValueChange = { aiSubjectName = it },
                        label = { Text("Subject (e.g. Modern Physics, Calculus)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = aiExamTarget,
                        onValueChange = { aiExamTarget = it },
                        label = { Text("Target Exam (e.g. SAT, JEE, AP)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (aiMessage != null) {
                        Text(
                            text = aiMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (aiSubjectName.isNotBlank() && !isAiGenerating) {
                            isAiGenerating = true
                            aiMessage = "Generating comprehensive syllabus structure with AI..."
                            viewModel.createSyllabusWithAi(aiSubjectName, aiExamTarget) { success, msg ->
                                isAiGenerating = false
                                aiMessage = msg
                                if (success) {
                                    showAiSyllabusDialog = false
                                }
                            }
                        }
                    },
                    enabled = !isAiGenerating && aiSubjectName.isNotBlank()
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generating...")
                    } else {
                        Text("Generate Curriculum")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAiSyllabusDialog = false },
                    enabled = !isAiGenerating
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TopicItemRow(
    topic: TopicEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAskAi: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = topic.isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = topic.difficultyLevel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    if (topic.isWeak) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ForgeRose.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "WEAK CONCEPT",
                                color = ForgeRose,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (topic.isCompleted) "Completed" else "Pending",
                        fontSize = 10.sp,
                        color = if (topic.isCompleted) ForgeEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onAskAi, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Ask AI",
                    tint = ForgeIndigoLight,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Topic",
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Topic",
                    tint = ForgeRose,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
