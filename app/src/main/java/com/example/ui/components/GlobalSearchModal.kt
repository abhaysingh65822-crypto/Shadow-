package com.example.ui.components

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
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

data class SearchResult(
    val category: String,
    val title: String,
    val subtitle: String,
    val targetRoute: StudyForgeRoute
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchModal(
    viewModel: StudyForgeViewModel,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var query by remember { mutableStateOf("") }

    val allNotes by viewModel.notes.collectAsStateWithLifecycle()
    val allQuestions by viewModel.questions.collectAsStateWithLifecycle()
    val allFlashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val allMistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val allChapters by viewModel.chapters.collectAsStateWithLifecycle()
    val allSubjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allTests by viewModel.tests.collectAsStateWithLifecycle()

    val searchResults = remember(query, allNotes, allQuestions, allFlashcards, allMistakes, allChapters, allSubjects, allTests) {
        val q = query.trim()
        if (q.length < 2) emptyList()
        else {
            val list = mutableListOf<SearchResult>()

            // Search Subjects
            allSubjects.filter { it.name.contains(q, ignoreCase = true) || it.code.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("SUBJECT", it.name, "${it.code} • ${it.totalChapters} chapters", StudyForgeRoute.Syllabus))
                }

            // Search Tests
            allTests.filter { it.title.contains(q, ignoreCase = true) || it.subjectFilter.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("EXAM", it.title, "${it.totalQuestions} questions • ${it.durationMinutes}m", StudyForgeRoute.TestEngine))
                }

            // Search Notes
            allNotes.filter { it.title.contains(q, ignoreCase = true) || it.contentMarkdown.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("NOTE", it.title, it.subjectName, StudyForgeRoute.Notes))
                }

            // Search Questions
            allQuestions.filter { it.questionText.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("QUESTION", it.questionText.take(60), "${it.difficulty} • PYQ: ${it.isPyq}", StudyForgeRoute.QuestionBank))
                }

            // Search Flashcards
            allFlashcards.filter { it.front.contains(q, ignoreCase = true) || it.back.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("FLASHCARD", it.front, "Interval: ${it.intervalDays}d", StudyForgeRoute.Flashcards))
                }

            // Search Mistakes
            allMistakes.filter { it.questionText.contains(q, ignoreCase = true) || it.explanation.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("MISTAKE", it.questionText.take(60), "${it.category} error in ${it.subjectName}", StudyForgeRoute.MistakeBook))
                }

            // Search Chapters
            allChapters.filter { it.title.contains(q, ignoreCase = true) }
                .take(3)
                .forEach {
                    list.add(SearchResult("SYLLABUS", it.title, "Mastery: ${it.masteryLevel}%", StudyForgeRoute.Syllabus))
                }

            list
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("global_search_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ForgeIndigoLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Global StudyForge Search",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search syllabus, notes, questions, cards...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (query.isNotBlank() && searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching items found for \"$query\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    viewModel.navigateTo(result.targetRoute)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = ForgeIndigoLight.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = result.category,
                                        color = ForgeIndigoLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = result.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                    Text(text = result.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
