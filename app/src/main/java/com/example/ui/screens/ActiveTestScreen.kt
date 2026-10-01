package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.theme.*
import org.json.JSONArray

@Composable
fun ActiveTestScreen(viewModel: StudyForgeViewModel) {
    val state by viewModel.activeTestState.collectAsStateWithLifecycle()
    var showSubmitConfirm by remember { mutableStateOf(false) }

    BackHandler {
        if (state?.isSubmitted == false) {
            showSubmitConfirm = true
        } else {
            viewModel.navigateTo(StudyForgeRoute.TestEngine)
        }
    }

    if (state == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active test in progress.")
        }
        return
    }

    val activeTest = state!!

    if (activeTest.isSubmitted) {
        // ==========================================
        // Test Result Review Screen
        // ==========================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("test_result_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TEST COMPLETED & GRADED",
                            style = MaterialTheme.typography.labelSmall,
                            color = ForgeIndigoLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeTest.test.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Score Badge
                        Surface(
                            shape = CircleShape,
                            color = if (activeTest.score >= activeTest.maxScore * 0.7) ForgeEmerald.copy(alpha = 0.15f) else ForgeAmber.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, if (activeTest.score >= activeTest.maxScore * 0.7) ForgeEmerald else ForgeAmber),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${activeTest.score}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (activeTest.score >= activeTest.maxScore * 0.7) ForgeEmerald else ForgeAmber
                                    )
                                    Text(
                                        text = "out of ${activeTest.maxScore}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stat Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${activeTest.correctCount}", fontWeight = FontWeight.Bold, color = ForgeEmerald)
                                Text(text = "Correct", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${activeTest.wrongCount}", fontWeight = FontWeight.Bold, color = ForgeRose)
                                Text(text = "Incorrect", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${activeTest.skippedCount}", fontWeight = FontWeight.Bold)
                                Text(text = "Skipped", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(StudyForgeRoute.MistakeBook) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeRose)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Mistakes", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(StudyForgeRoute.Dashboard) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dashboard", fontSize = 13.sp)
                    }
                }
            }

            item {
                Text(
                    text = "Question-by-Question Solution Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(activeTest.questions) { index, q ->
                val userAns = activeTest.selectedAnswers[index]
                val isCorrect = userAns != null && userAns.trim().equals(q.correctAnswer.trim(), ignoreCase = true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCorrect) ForgeEmerald.copy(alpha = 0.4f) else ForgeRose.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Q${index + 1} (${q.difficulty})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isCorrect) ForgeEmerald else ForgeRose
                            )
                            Text(
                                text = if (isCorrect) "+${q.marks} Marks" else if (userAns != null) "-${q.negativeMarks} Marks" else "0 (Skipped)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = q.questionText, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your Answer: ${userAns ?: "Not Attempted"}",
                            fontSize = 12.sp,
                            color = if (isCorrect) ForgeEmerald else ForgeRose,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Correct Answer: ${q.correctAnswer}",
                            fontSize = 12.sp,
                            color = ForgeEmerald,
                            fontWeight = FontWeight.Bold
                        )

                        if (q.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Explanation: ${q.explanation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // ==========================================
    // Active Timed Exam Screen
    // ==========================================
    val currentQuestion = activeTest.questions.getOrNull(activeTest.currentIndex)
    val remainingMins = activeTest.remainingSeconds / 60
    val remainingSecs = activeTest.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", remainingMins, remainingSecs)

    val options = remember(currentQuestion) {
        if (currentQuestion == null) emptyList()
        else {
            try {
                val jsonArray = JSONArray(currentQuestion.optionsJson)
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getString(i))
                }
                list
            } catch (e: Exception) {
                listOf("Option A", "Option B", "Option C", "Option D")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_test_screen")
    ) {
        // Top Sticky Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Question ${activeTest.currentIndex + 1} of ${activeTest.questions.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activeTest.test.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Countdown Timer
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeTest.remainingSeconds < 300) ForgeRose.copy(alpha = 0.15f) else ForgeCyan.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (activeTest.remainingSeconds < 300) ForgeRose else ForgeCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeFormatted,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (activeTest.remainingSeconds < 300) ForgeRose else ForgeCyan,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = { showSubmitConfirm = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeEmerald),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("submit_test_btn")
                ) {
                    Text("Submit", fontSize = 12.sp)
                }
            }
        }

        // Horizontal Question Palette
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeTest.questions.size) { idx ->
                val isAnswered = activeTest.selectedAnswers.containsKey(idx)
                val isMarked = activeTest.markedForReview.contains(idx)
                val isCurrent = activeTest.currentIndex == idx

                val paletteBg = when {
                    isCurrent -> ForgeIndigo
                    isMarked -> ForgeAmber
                    isAnswered -> ForgeEmerald
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = if (isCurrent || isMarked || isAnswered) Color.White else MaterialTheme.colorScheme.onSurface

                Surface(
                    shape = CircleShape,
                    color = paletteBg,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { viewModel.setTestQuestionIndex(idx) },
                    border = if (isCurrent) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${idx + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Question Details Area
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                if (currentQuestion != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = ForgeIndigoLight.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = currentQuestion.difficulty,
                                        color = ForgeIndigoLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "+${currentQuestion.marks} / -${currentQuestion.negativeMarks} Marks",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // Options
            items(options) { opt ->
                val currentSelected = activeTest.selectedAnswers[activeTest.currentIndex]
                val isSelected = currentSelected == opt

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectTestAnswer(activeTest.currentIndex, opt) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ForgeIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) ForgeIndigoLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectTestAnswer(activeTest.currentIndex, opt) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Bottom Navigation Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (activeTest.currentIndex > 0) {
                            viewModel.setTestQuestionIndex(activeTest.currentIndex - 1)
                        }
                    },
                    enabled = activeTest.currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev")
                }

                // Mark for review button
                val isMarked = activeTest.markedForReview.contains(activeTest.currentIndex)
                TextButton(
                    onClick = { viewModel.toggleMarkForReview(activeTest.currentIndex) }
                ) {
                    Icon(
                        imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isMarked) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMarked) "Marked" else "Mark for Review",
                        color = if (isMarked) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (activeTest.currentIndex < activeTest.questions.size - 1) {
                            viewModel.setTestQuestionIndex(activeTest.currentIndex + 1)
                        } else {
                            showSubmitConfirm = true
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (activeTest.currentIndex == activeTest.questions.size - 1) "Review & Submit" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Submit Examination?") },
            text = {
                Text("You have answered ${activeTest.selectedAnswers.size} of ${activeTest.questions.size} questions. Are you sure you wish to submit and finalize your test score?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        viewModel.submitActiveTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeEmerald)
                ) {
                    Text("Yes, Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Continue Test")
                }
            }
        )
    }
}
