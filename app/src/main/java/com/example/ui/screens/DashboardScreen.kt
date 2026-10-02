package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.components.TaskCompletionChartComponent
import com.example.ui.theme.*

@Composable
fun DashboardScreen(viewModel: StudyForgeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val weakTopics by viewModel.weakTopics.collectAsStateWithLifecycle()
    val testAttempts by viewModel.testAttempts.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()
    val plannerTasks by viewModel.plannerTasks.collectAsStateWithLifecycle()

    val completedStudyMinutes = remember(viewModel.studySessions) {
        viewModel.studySessions.value.sumOf { it.durationMinutes }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Hero Header: Level, Streak, XP, Exam Countdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STUDYFORGE OS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForgeIndigoLight,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Personal Command Center",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ForgeAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForgeAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = ForgeAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile?.currentStreak ?: 0} Day Streak",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ForgeAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP and Level Bar
                    val currentXp = profile?.totalXp ?: 0
                    val currentLevel = profile?.level ?: 1
                    val xpInLevel = currentXp % 300
                    val progressFloat = (xpInLevel / 300f).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level $currentLevel Scholar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$currentXp XP Total (${xpInLevel}/300 to Lvl ${currentLevel + 1})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ForgeIndigoLight,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Exam Countdown Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(StudyForgeRoute.Profile) },
                        shape = RoundedCornerShape(12.dp),
                        color = ForgeCyan.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = ForgeCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (!profile?.examTargetName.isNullOrBlank()) profile!!.examTargetName else "Set Target Exam",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (profile?.examDateMillis != null && profile!!.examDateMillis!! > System.currentTimeMillis()) {
                                    val days = ((profile!!.examDateMillis!! - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
                                    "$days Days Left"
                                } else if (!profile?.examTargetName.isNullOrBlank()) {
                                    "Date Not Set"
                                } else {
                                    "Tap to Set"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForgeCyan
                            )
                        }
                    }
                }
            }
        }

        // Daily AI Briefing & Adaptive Recommendation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ForgeIndigo.copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = ForgeIndigoLight,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily AI Study Intelligence",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val weakCount = weakTopics.size
                    val totalSessions = viewModel.studySessions.value.size
                    val recText = if (weakCount > 0) {
                        "Your performance telemetry highlights ${weakCount} weak concept(s), starting with \"${weakTopics.firstOrNull()?.title}\". Recommended action: Complete a 20-minute targeted focus session to strengthen foundation."
                    } else if (dueCards.isNotEmpty()) {
                        "You have ${dueCards.size} flashcards due for active recall today under spaced repetition. Review them to prevent memory decay."
                    } else if (totalSessions == 0) {
                        "Welcome to StudyForge! Complete a focus session or answer questions in the Question Bank to build your personalized study telemetry and adaptive AI recommendations."
                    } else {
                        "Syllabus on track! Recommended action: Generate a 10-question practice set or diagnostic mock exam to test retention under exam conditions."
                    }

                    Text(
                        text = recText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.navigateTo(StudyForgeRoute.Pomodoro) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_focus_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Focus", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(StudyForgeRoute.QuestionBank) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_practice_button"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Practice Weak", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Task Completion Analytics & Visualizer Chart
        item {
            TaskCompletionChartComponent(
                tasks = plannerTasks,
                onOpenPlanner = { viewModel.navigateTo(StudyForgeRoute.Planner) },
                onGenerateAiPlan = {
                    viewModel.generateAiStudyPlan { _, _ -> }
                }
            )
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "Operational Modules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModuleTile(
                    modifier = Modifier.weight(1f),
                    title = "Syllabus",
                    subtitle = "${subjects.size} Subjects",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = ForgeIndigoLight,
                    testTag = "tile_syllabus",
                    onClick = { viewModel.navigateTo(StudyForgeRoute.Syllabus) }
                )
                ModuleTile(
                    modifier = Modifier.weight(1f),
                    title = "Test Engine",
                    subtitle = "${testAttempts.size} Taken",
                    icon = Icons.Default.Quiz,
                    accentColor = ForgeCyan,
                    testTag = "tile_tests",
                    onClick = { viewModel.navigateTo(StudyForgeRoute.TestEngine) }
                )
                ModuleTile(
                    modifier = Modifier.weight(1f),
                    title = "Flashcards",
                    subtitle = "${dueCards.size} Due",
                    icon = Icons.Default.Style,
                    accentColor = ForgeAmber,
                    testTag = "tile_flashcards",
                    onClick = { viewModel.navigateTo(StudyForgeRoute.Flashcards) }
                )
                ModuleTile(
                    modifier = Modifier.weight(1f),
                    title = "Mistakes",
                    subtitle = "${viewModel.mistakes.value.size} Logged",
                    icon = Icons.Default.ErrorOutline,
                    accentColor = ForgeRose,
                    testTag = "tile_mistakes",
                    onClick = { viewModel.navigateTo(StudyForgeRoute.MistakeBook) }
                )
            }
        }

        // Subject Progress Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subject Mastery Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.navigateTo(StudyForgeRoute.Syllabus) }) {
                    Text("View All")
                }
            }
        }

        items(subjects) { subject ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(StudyForgeRoute.Syllabus) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(android.graphics.Color.parseColor(subject.colorHex)).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color(android.graphics.Color.parseColor(subject.colorHex)),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${subject.code} • ${subject.completedChapters}/${subject.totalChapters} Chapters Done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val pct = if (subject.totalChapters > 0) {
                            ((subject.completedChapters.toFloat() / subject.totalChapters) * 100).toInt()
                        } else 0

                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(android.graphics.Color.parseColor(subject.colorHex))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val pctFloat = if (subject.totalChapters > 0) {
                        (subject.completedChapters.toFloat() / subject.totalChapters)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { pctFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(android.graphics.Color.parseColor(subject.colorHex)),
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Weak Topics Section
        if (weakTopics.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ForgeRose.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForgeRose.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Weak Areas",
                                tint = ForgeRose,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attention Required (${weakTopics.size} Weak Topics)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ForgeRose
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        weakTopics.take(3).forEach { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${topic.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Accuracy: ${topic.accuracyRate}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForgeRose,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
