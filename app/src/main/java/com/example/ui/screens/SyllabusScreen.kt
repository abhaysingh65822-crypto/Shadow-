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

    var selectedSubjectId by remember { mutableStateOf(1L) }
    var expandedChapterId by remember { mutableStateOf<Long?>(1L) }
    val coroutineScope = rememberCoroutineScope()

    val currentSubjectChapters = remember(allChapters, selectedSubjectId) {
        allChapters.filter { it.subjectId == selectedSubjectId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("syllabus_screen")
    ) {
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
        }

        // Chapters & Topics List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hierarchical Syllabus & Mastery",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentSubjectChapters.size} Chapters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Mastery: ${chapter.masteryLevel}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (chapter.masteryLevel >= 75) ForgeEmerald else ForgeAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = "•", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${chapter.timeSpentMinutes} mins studied",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { expandedChapterId = if (isExpanded) null else chapter.id }
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { chapter.completionPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ForgeIndigoLight,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Topics & Competencies",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (chapterTopics.isEmpty()) {
                                    Text(
                                        text = "Foundational concepts integrated into syllabus core.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    chapterTopics.forEach { topic ->
                                        TopicItemRow(
                                            topic = topic,
                                            onToggleComplete = {
                                                coroutineScope.launch {
                                                    viewModel.repository.updateTopic(
                                                        topic.copy(isCompleted = !topic.isCompleted)
                                                    )
                                                }
                                            },
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

                                // Chapter Action Sheet
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.navigateTo(StudyForgeRoute.QuestionBank)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Questions", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.selectedBot.value = AiBotType.AI_PHYSICS_TUTOR
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

@Composable
fun TopicItemRow(
    topic: TopicEntity,
    onToggleComplete: () -> Unit,
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
                        text = "Accuracy: ${topic.accuracyRate}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onAskAi, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Ask AI",
                    tint = ForgeIndigoLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
