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

    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var selectedDifficultyFilter by remember { mutableStateOf("ALL") }
    var showPyqOnly by remember { mutableStateOf(false) }

    var showAiGenerateDialog by remember { mutableStateOf(false) }
    var generateTopic by remember { mutableStateOf("") }
    var generateDifficulty by remember { mutableStateOf("MEDIUM") }
    var generateType by remember { mutableStateOf("MCQ") }
    var isGenerating by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val filteredQuestions = remember(allQuestions, selectedSubjectId, selectedDifficultyFilter, showPyqOnly) {
        allQuestions.filter { q ->
            val matchSubject = selectedSubjectId == null || q.subjectId == selectedSubjectId
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
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text("All Subjects") }
                    )
                }
                items(subjects) { subject ->
                    FilterChip(
                        selected = selectedSubjectId == subject.id,
                        onClick = { selectedSubjectId = if (selectedSubjectId == subject.id) null else subject.id },
                        label = { Text(subject.name) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedDifficultyFilter == "ALL",
                        onClick = {
                            selectedDifficultyFilter = when (selectedDifficultyFilter) {
                                "ALL" -> "EASY"
                                "EASY" -> "MEDIUM"
                                "MEDIUM" -> "HARD"
                                else -> "ALL"
                            }
                        },
                        label = { Text("Diff: $selectedDifficultyFilter") }
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
                    Column {
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

                    Button(
                        onClick = { showAiGenerateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Generate", fontSize = 12.sp)
                    }
                }
            }

            items(filteredQuestions) { question ->
                QuestionPracticeCard(
                    question = question,
                    onAttempt = { answer, onVerified ->
                        viewModel.attemptQuestionInBank(question, answer, onVerified)
                    },
                    onBookmark = {
                        coroutineScope.launch {
                            viewModel.repository.toggleBookmark(
                                type = "QUESTION",
                                id = question.id,
                                title = question.questionText.take(60),
                                subtitle = "${question.difficulty} • PYQ: ${question.isPyq}",
                                route = "question_bank",
                                isCurrentlyBookmarked = question.isBookmarked
                            )
                        }
                    }
                )
            }
        }
    }

    if (showAiGenerateDialog) {
        val targetSubject = subjects.firstOrNull { it.id == selectedSubjectId } ?: subjects.firstOrNull()
        AlertDialog(
            onDismissRequest = { if (!isGenerating) showAiGenerateDialog = false },
            title = { Text("AI Question Generator") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Generate structured test questions for ${targetSubject?.name ?: "your subjects"} with answer keys and full explanations.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = generateTopic,
                        onValueChange = { generateTopic = it },
                        label = { Text("Topic or Concept") },
                        placeholder = { Text("e.g. Projectile Motion or Stoichiometry") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                            FilterChip(
                                selected = generateDifficulty == diff,
                                onClick = { generateDifficulty = diff },
                                label = { Text(diff, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MCQ", "NUMERICAL", "TRUE_FALSE").forEach { t ->
                            FilterChip(
                                selected = generateType == t,
                                onClick = { generateType = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (isGenerating) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (generationStatus != null) {
                        Text(
                            text = generationStatus ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeEmerald
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetSubject != null && generateTopic.isNotBlank()) {
                            isGenerating = true
                            generationStatus = "Generating questions..."
                            viewModel.generateQuestionsWithAi(
                                subjectId = targetSubject.id,
                                chapterId = 1L,
                                topicId = 1L,
                                subject = targetSubject.name,
                                topic = generateTopic.trim(),
                                difficulty = generateDifficulty,
                                type = generateType,
                                count = 3
                            ) { ok, msg ->
                                isGenerating = false
                                generationStatus = msg
                                if (ok) {
                                    showAiGenerateDialog = false
                                    generateTopic = ""
                                }
                            }
                        }
                    },
                    enabled = !isGenerating && generateTopic.isNotBlank()
                ) {
                    Text("Generate & Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAiGenerateDialog = false },
                    enabled = !isGenerating
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QuestionPracticeCard(
    question: QuestionEntity,
    onAttempt: (String, (Boolean) -> Unit) -> Unit,
    onBookmark: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var numericalInput by remember { mutableStateOf("") }
    var hasAnswered by remember { mutableStateOf(false) }
    var isCorrectAnswer by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(question.isBookmarked) }

    val isNumerical = question.type.equals("NUMERICAL", ignoreCase = true) || question.optionsJson == "[]"

    val options = remember(question.optionsJson) {
        try {
            val jsonArray = JSONArray(question.optionsJson)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            if (!isNumerical) listOf("Option A", "Option B", "Option C", "Option D") else emptyList()
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
            // Badges & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = question.type,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${question.marks} / -${question.negativeMarks}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            onBookmark()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) ForgeAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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

            // Options or Numerical Input
            if (isNumerical || options.isEmpty()) {
                OutlinedTextField(
                    value = if (hasAnswered) selectedOption ?: "" else numericalInput,
                    onValueChange = {
                        if (!hasAnswered) {
                            numericalInput = it
                            selectedOption = it
                        }
                    },
                    label = { Text("Your Calculated Value / Answer") },
                    placeholder = { Text("e.g. 9.8 or 42") },
                    enabled = !hasAnswered,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            } else {
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
