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
import com.example.data.QuestionEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
fun QuestionBankScreen(viewModel: StudyForgeViewModel) {
    val allQuestions by viewModel.questions.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var selectedDifficultyFilter by remember { mutableStateOf("ALL") }
    var showPyqOnly by remember { mutableStateOf(false) }

    val filteredQuestions = remember(allQuestions, selectedSubjectFilter, selectedDifficultyFilter, showPyqOnly) {
        allQuestions.filter { q ->
            val matchSubject = when (selectedSubjectFilter) {
                "ALL" -> true
                "PHYSICS" -> q.subjectId == 1L
                "CHEMISTRY" -> q.subjectId == 2L
                "MATH" -> q.subjectId == 3L
                "CS" -> q.subjectId == 4L
                else -> true
            }
            val matchDiff = if (selectedDifficultyFilter == "ALL") true else q.difficulty.equals(selectedDifficultyFilter, ignoreCase = true)
            val matchPyq = if (showPyqOnly) q.isPyq else true
            matchSubject && matchDiff && matchPyq
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("question_bank_screen")
    ) {
        // Filter Chips Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectFilter == "ALL",
                        onClick = { selectedSubjectFilter = "ALL" },
                        label = { Text("All Subjects") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedSubjectFilter == "PHYSICS",
                        onClick = { selectedSubjectFilter = "PHYSICS" },
                        label = { Text("Physics") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedSubjectFilter == "CHEMISTRY",
                        onClick = { selectedSubjectFilter = "CHEMISTRY" },
                        label = { Text("Chemistry") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedSubjectFilter == "MATH",
                        onClick = { selectedSubjectFilter = "MATH" },
                        label = { Text("Mathematics") }
                    )
                }
                item {
                    FilterChip(
                        selected = showPyqOnly,
                        onClick = { showPyqOnly = !showPyqOnly },
                        label = { Text("PYQ Only") },
                        leadingIcon = {
                            if (showPyqOnly) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        // Questions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Practice Repository",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredQuestions.size} Questions Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(filteredQuestions) { question ->
                QuestionPracticeCard(
                    question = question,
                    onAttempt = { answer, onVerified ->
                        viewModel.attemptQuestionInBank(question, answer, onVerified)
                    },
                    onBookmark = {
                        // toggle bookmark
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionPracticeCard(
    question: QuestionEntity,
    onAttempt: (String, (Boolean) -> Unit) -> Unit,
    onBookmark: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var isCorrectAnswer by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    val options = remember(question.optionsJson) {
        try {
            val jsonArray = JSONArray(question.optionsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            listOf("Option A", "Option B", "Option C", "Option D")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (question.difficulty) {
                            "HARD" -> ForgeRose.copy(alpha = 0.15f)
                            "EASY" -> ForgeEmerald.copy(alpha = 0.15f)
                            else -> ForgeAmber.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = question.difficulty,
                            color = when (question.difficulty) {
                                "HARD" -> ForgeRose
                                "EASY" -> ForgeEmerald
                                else -> ForgeAmber
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (question.isPyq) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ForgeIndigoLight.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PYQ ${question.year}",
                                color = ForgeIndigoLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "+${question.marks} / -${question.negativeMarks}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options
            options.forEach { option ->
                val isSelected = selectedOption == option
                val optionBorderColor = when {
                    hasAnswered && option == question.correctAnswer -> ForgeEmerald
                    hasAnswered && isSelected && !isCorrectAnswer -> ForgeRose
                    isSelected -> ForgeIndigoLight
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                val optionBgColor = when {
                    hasAnswered && option == question.correctAnswer -> ForgeEmerald.copy(alpha = 0.1f)
                    hasAnswered && isSelected && !isCorrectAnswer -> ForgeRose.copy(alpha = 0.1f)
                    isSelected -> ForgeIndigo.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !hasAnswered) {
                            selectedOption = option
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = optionBgColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, optionBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { if (!hasAnswered) selectedOption = option },
                            enabled = !hasAnswered
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions (Submit / Hint / Explanation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (question.hint.isNotBlank()) {
                        TextButton(
                            onClick = { showHint = !showHint },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint", fontSize = 12.sp)
                        }
                    }

                    if (hasAnswered && question.explanation.isNotBlank()) {
                        TextButton(
                            onClick = { showExplanation = !showExplanation },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Solution", fontSize = 12.sp)
                        }
                    }
                }

                if (!hasAnswered) {
                    Button(
                        onClick = {
                            selectedOption?.let { chosen ->
                                onAttempt(chosen) { isCorrect ->
                                    hasAnswered = true
                                    isCorrectAnswer = isCorrect
                                    if (!isCorrect) showExplanation = true
                                }
                            }
                        },
                        enabled = selectedOption != null,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Check Answer", fontSize = 12.sp)
                    }
                } else {
                    Text(
                        text = if (isCorrectAnswer) "✓ Correct (+${question.marks} XP)" else "✕ Incorrect (Logged to Mistakes)",
                        color = if (isCorrectAnswer) ForgeEmerald else ForgeRose,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(visible = showHint) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ForgeAmber.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Hint: ${question.hint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showExplanation) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Detailed Solution & Step Derivation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ForgeIndigoLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
