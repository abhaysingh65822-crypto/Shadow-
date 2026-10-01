package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ai.AiBotType
import com.example.data.MistakeEntity
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MistakeBookScreen(viewModel: StudyForgeViewModel) {
    val allMistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    val coroutineScope = rememberCoroutineScope()

    val filteredMistakes = remember(allMistakes, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") allMistakes
        else allMistakes.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mistake_book_screen")
    ) {
        // Filter Chips Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Mistake Registry & Error Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "ALL",
                        onClick = { selectedCategoryFilter = "ALL" },
                        label = { Text("All (${allMistakes.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "CONCEPTUAL",
                        onClick = { selectedCategoryFilter = "CONCEPTUAL" },
                        label = { Text("Conceptual") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "CALCULATION",
                        onClick = { selectedCategoryFilter = "CALCULATION" },
                        label = { Text("Calculation") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "MEMORY",
                        onClick = { selectedCategoryFilter = "MEMORY" },
                        label = { Text("Memory") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == "CARELESS",
                        onClick = { selectedCategoryFilter = "CARELESS" },
                        label = { Text("Careless") }
                    )
                }
            }
        }

        if (filteredMistakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = ForgeEmerald,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Clean Record!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No recorded mistakes in this category. Keep solving practice sets!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredMistakes) { mistake ->
                    MistakeCard(
                        mistake = mistake,
                        onResolve = {
                            coroutineScope.launch {
                                viewModel.repository.updateMistake(
                                    mistake.copy(reviewStatus = "RESOLVED")
                                )
                            }
                        },
                        onAskAi = {
                            viewModel.selectedBot.value = AiBotType.AI_REVISION_COACH
                            viewModel.aiChatInput.value = "Help me understand this mistake in ${mistake.topicName}: Question: ${mistake.questionText}. I answered '${mistake.userAnswer}', but correct is '${mistake.correctAnswer}'."
                            viewModel.navigateTo(StudyForgeRoute.AiBots)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MistakeCard(
    mistake: MistakeEntity,
    onResolve: () -> Unit,
    onAskAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mistake_card_${mistake.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForgeRose.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ForgeRose.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${mistake.category} ERROR",
                        color = ForgeRose,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${mistake.subjectName} • ${mistake.topicName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = mistake.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Your Answer: ${mistake.userAnswer}",
                        color = ForgeRose,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Correct Answer: ${mistake.correctAnswer}",
                        color = ForgeEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (mistake.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Analysis: ${mistake.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAskAi,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Diagnosis", fontSize = 12.sp)
                }

                Button(
                    onClick = onResolve,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeEmerald),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resolved", fontSize = 12.sp)
                }
            }
        }
    }
}
