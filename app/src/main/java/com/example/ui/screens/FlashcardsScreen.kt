package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.DeckEntity
import com.example.data.FlashcardEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FlashcardsScreen(viewModel: StudyForgeViewModel) {
    val allCards by viewModel.flashcards.collectAsStateWithLifecycle()
    val dueCards by viewModel.dueFlashcards.collectAsStateWithLifecycle()
    val decks by viewModel.decks.collectAsStateWithLifecycle()
    val cardIndex by viewModel.activeCardIndex.collectAsStateWithLifecycle()
    val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()

    var selectedDeckId by remember { mutableStateOf<Long?>(null) }
    var showAiGeneratorDialog by remember { mutableStateOf(false) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var showAddDeckDialog by remember { mutableStateOf(false) }
    var newDeckName by remember { mutableStateOf("") }
    var newDeckDesc by remember { mutableStateOf("") }

    var editingCard by remember { mutableStateOf<FlashcardEntity?>(null) }
    var editFront by remember { mutableStateOf("") }
    var editBack by remember { mutableStateOf("") }

    var manualFront by remember { mutableStateOf("") }
    var manualBack by remember { mutableStateOf("") }
    var generatorTopic by remember { mutableStateOf("") }
    var isAiGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val deckFilteredCards = remember(allCards, selectedDeckId) {
        if (selectedDeckId == null) allCards
        else allCards.filter { it.deckId == selectedDeckId }
    }

    val currentCards = if (selectedDeckId != null) deckFilteredCards else (if (dueCards.isNotEmpty()) dueCards else allCards)
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
                                text = "${dueCards.size} Card(s) Due for Review Today",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showManualAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Card", fontSize = 12.sp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Decks Filter & Management Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Decks",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { showAddDeckDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+ New Deck", fontSize = 11.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDeckId == null,
                                onClick = { selectedDeckId = null },
                                label = { Text("All Decks (${allCards.size})") }
                            )
                        }
                        items(decks) { deck ->
                            val count = allCards.count { it.deckId == deck.id }
                            FilterChip(
                                selected = selectedDeckId == deck.id,
                                onClick = { selectedDeckId = deck.id },
                                label = { Text("${deck.name} ($count)") }
                            )
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
                        Text(
                            text = if (allCards.isEmpty()) "No flashcards in library. Tap '+ Card' to create one!" else "All flashcard reviews completed for now!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // All Cards in Deck Listing
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stored Cards (${deckFilteredCards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(deckFilteredCards) { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    Row {
                        IconButton(
                            onClick = {
                                editingCard = card
                                editFront = card.front
                                editBack = card.back
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Card",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.repository.deleteFlashcard(card.id)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Flashcard",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Manual Add Dialog
    if (showManualAddDialog) {
        AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            title = { Text("Create Flashcard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = manualFront,
                        onValueChange = { manualFront = it },
                        label = { Text("Front (Prompt / Question / Formula)") },
                        placeholder = { Text("e.g. Formula for kinetic energy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualBack,
                        onValueChange = { manualBack = it },
                        label = { Text("Back (Answer / Derivation)") },
                        placeholder = { Text("e.g. KE = 1/2 * m * v^2") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualFront.isNotBlank() && manualBack.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.repository.insertFlashcard(
                                    FlashcardEntity(
                                        deckId = selectedDeckId ?: 1L,
                                        front = manualFront.trim(),
                                        back = manualBack.trim(),
                                        cardType = "STANDARD"
                                    )
                                )
                                showManualAddDialog = false
                                manualFront = ""
                                manualBack = ""
                            }
                        }
                    },
                    enabled = manualFront.isNotBlank() && manualBack.isNotBlank()
                ) {
                    Text("Save Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Card Dialog
    if (editingCard != null) {
        AlertDialog(
            onDismissRequest = { editingCard = null },
            title = { Text("Edit Flashcard") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editFront,
                        onValueChange = { editFront = it },
                        label = { Text("Front (Question)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBack,
                        onValueChange = { editBack = it },
                        label = { Text("Back (Answer)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingCard?.let { card ->
                            viewModel.editFlashcard(card.copy(front = editFront.trim(), back = editBack.trim()))
                        }
                        editingCard = null
                    },
                    enabled = editFront.isNotBlank() && editBack.isNotBlank()
                ) {
                    Text("Update Card")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCard = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Deck Dialog
    if (showAddDeckDialog) {
        AlertDialog(
            onDismissRequest = { showAddDeckDialog = false },
            title = { Text("Create Deck") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newDeckName,
                        onValueChange = { newDeckName = it },
                        label = { Text("Deck Name") },
                        placeholder = { Text("e.g. Thermodynamics Formulas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDeckDesc,
                        onValueChange = { newDeckDesc = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g. High-yield exam recall cards") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDeckName.isNotBlank()) {
                            viewModel.createDeck(newDeckName, subjectId = 1L, description = newDeckDesc)
                            showAddDeckDialog = false
                            newDeckName = ""
                            newDeckDesc = ""
                        }
                    },
                    enabled = newDeckName.isNotBlank()
                ) {
                    Text("Create Deck")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDeckDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                                viewModel.repository.insertFlashcard(
                                    FlashcardEntity(
                                        deckId = selectedDeckId ?: 1L,
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
