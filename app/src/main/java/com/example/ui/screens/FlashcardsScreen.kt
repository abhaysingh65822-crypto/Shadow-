package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.FlashcardEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FlashcardsScreen(viewModel: StudyForgeViewModel) {
    val allCards by viewModel.flashcards.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()
    val cardIndex by viewModel.activeCardIndex.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()

    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var generatorTopic by remember { mutableStateOf("") }
    var isAiGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val currentCards = if (dueCards.isNotEmpty()) dueCards else allCards
    val activeCard = currentCards.getOrNull(cardIndex.coerceIn(0, (currentCards.size - 1).coerceAtLeast(0)))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("flashcards_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deck Queue Stats Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeAmber.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Spaced Repetition Engine (SM-2)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dueCards.size} Cards Due for Review Today",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { showAiGeneratorDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Generate", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Interactive Flashcard
        item {
            if (activeCard != null) {
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(400),
                    label = "cardFlip"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { viewModel.isCardFlipped.value = !isFlipped }
                        .testTag("flashcard_flip_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) ForgeIndigoDark else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        if (isFlipped) ForgeCyanLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (!isFlipped) "QUESTION (TAP TO FLIP)" else "ANSWER & FORMULA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlipped) ForgeCyanLight else ForgeIndigoLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (!isFlipped) activeCard.front else activeCard.back,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFlipped) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.graphicsLayer {
                                    // Invert text horizontally when rotated > 90 deg so it renders upright
                                    if (rotation > 90f) rotationY = 180f
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Interval: ${activeCard.intervalDays}d • Ease: ${String.format("%.1f", activeCard.easeFactor)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFlipped) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SM-2 Review Rating Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.rateFlashcard(activeCard, 0) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeRose),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Again", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("< 1 Day", fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.rateFlashcard(activeCard, 1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeAmber),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("1 Day", fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.rateFlashcard(activeCard, 2) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigoLight),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Good", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("3 Days", fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { viewModel.rateFlashcard(activeCard, 3) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForgeEmerald),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Easy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("6 Days", fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("All flashcard reviews completed for now!")
                    }
                }
            }
        }

        // All Cards in Deck Listing
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All Stored Cards (${allCards.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(allCards) { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Q: ${card.front}",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A: ${card.back}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // AI Flashcard Generation Dialog
    if (showAiGeneratorDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAiGenerating) showAiGeneratorDialog = false },
            title = { Text("Generate Flashcards with AI") },
            text = {
                Column {
                    Text(
                        text = "Enter a topic, chapter, or formula to automatically generate high-yield active recall flashcards:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = generatorTopic,
                        onValueChange = { generatorTopic = it },
                        label = { Text("Topic / Concept") },
                        placeholder = { Text("e.g. Work-Energy Theorem & Power") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (isAiGenerating) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topic = generatorTopic.trim()
                        if (topic.isNotBlank()) {
                            isAiGenerating = true
                            coroutineScope.launch {
                                val res = viewModel.aiManager.generateResponse(
                                    botType = AiBotType.AI_FLASHCARD_GENERATOR,
                                    userPrompt = "Generate 2 atomic Q&A flashcards for topic: $topic"
                                )
                                // Insert newly created card into Room
                                viewModel.repository.insertFlashcard(
                                    FlashcardEntity(
                                        front = "What is the key principle of $topic?",
                                        back = res.text.take(200),
                                        cardType = "STANDARD"
                                    )
                                )
                                isAiGenerating = false
                                showAiGeneratorDialog = false
                                generatorTopic = ""
                            }
                        }
                    },
                    enabled = !isAiGenerating && generatorTopic.isNotBlank()
                ) {
                    Text("Generate & Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAiGeneratorDialog = false },
                    enabled = !isAiGenerating
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
