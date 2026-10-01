package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(viewModel: StudyForgeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val sessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val attempts by viewModel.testAttempts.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    val totalStudyMinutes = remember(sessions) { sessions.sumOf { it.durationMinutes } }
    val avgTestAccuracy = remember(attempts) {
        if (attempts.isNotEmpty()) attempts.map { it.accuracy }.average().toInt() else 0
    }
    val avgSessionLength = remember(sessions) {
        if (sessions.isNotEmpty()) sessions.map { it.durationMinutes }.average().toInt() else 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real Learning Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calculated exclusively from your active study sessions, tests, and question attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Total Study Time",
                    value = "${totalStudyMinutes / 60}h ${totalStudyMinutes % 60}m",
                    accentColor = ForgeIndigoLight
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Average Accuracy",
                    value = if (attempts.isNotEmpty()) "$avgTestAccuracy%" else "No data yet",
                    accentColor = ForgeEmerald
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Tests Evaluated",
                    value = "${attempts.size}",
                    accentColor = ForgeCyan
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "Active Mistakes",
                    value = "${mistakes.size}",
                    accentColor = ForgeRose
                )
            }
        }

        // Subject Study Time Distribution
        item {
            Text(
                text = "Subject Study Hours Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (totalStudyMinutes == 0) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "No study sessions recorded yet. Start a focus session in Pomodoro or practice questions to populate telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(subjects) { subject ->
            val subjectMins = remember(sessions, subject.name) {
                sessions.filter { it.subjectName.equals(subject.name, ignoreCase = true) }.sumOf { it.durationMinutes }
            }
            val progress = if (totalStudyMinutes > 0) (subjectMins.toFloat() / totalStudyMinutes) else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = subject.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (totalStudyMinutes > 0) "${subjectMins}m (${(progress * 100).toInt()}%)" else "0m",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(android.graphics.Color.parseColor(subject.colorHex))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
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

        // AI Diagnostics & Insights Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ForgeIndigo.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ForgeIndigoLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Learning Optimization Summary",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val streakText = if ((profile?.currentStreak ?: 0) > 0) {
                        "• Momentum is strong with a ${profile?.currentStreak}-day active study streak."
                    } else {
                        "• Start a study session today to establish your daily study streak."
                    }

                    val mistakeText = if (mistakes.isNotEmpty()) {
                        "• ${mistakes.size} mistake(s) logged in Mistake Book. Review them to convert weak spots into strengths."
                    } else {
                        "• Zero unresolved errors in Mistake Book. Maintain this standard in your practice tests."
                    }

                    val sessionText = if (avgSessionLength > 0) {
                        "• Average session length of $avgSessionLength minutes across ${sessions.size} logged focus blocks."
                    } else {
                        "• Log your first focus session using the Pomodoro timer to measure cognitive endurance."
                    }

                    Text(
                        text = "$streakText\n$mistakeText\n$sessionText",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
        }
    }
}
