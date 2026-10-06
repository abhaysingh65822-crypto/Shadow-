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
    var reviewAllMode by remember { mutableStateOf(false) }

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
    var generatorCount by remember { mutableIntStateOf(3) }
    var isAiGenerating by remember { mutableStateOf(false) }
    var aiStatusMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val deckFilteredCards = remember(allCards, selectedDeckId) {
        if (selectedDeckId == null) allCards
        else allCards.filter { it.deckId == selectedDeckId }
    }

    // Determine deck-specific due cards
    val activeCardsPool = remember(deckFilteredCards, dueCards, reviewAllMode, selectedDeckId) {
        if (reviewAllMode) {
            deckFilteredCards
        } else {
            val due = if (selectedDeckId == null) dueCards else dueCards.filter { it.deckId == selectedDeckId }
            if (due.isNotEmpty()) due else deckFilteredCards
        }
    }

    val activeCard = activeCardsPool.getOrNull(cardIndex.coerceIn(0, (activeCardsPool.size - 1).coerceAtLeast(0)))

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
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeAmber.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Spaced Repetition (SM-2)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dueCards.size} Due Today • ${allCards.size} Total Stored",
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
                                onClick = {
                                    aiStatusMessage = null
                                    showAiGeneratorDialog = true
                                },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle: Due Today vs Review All
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (reviewAllMode) "Mode: Studying all cards in deck" else "Mode: SM-2 Due Review",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterChip(
                            selected = reviewAllMode,
                            onClick = { reviewAllMode = !reviewAllMode },
                            label = { Text(if (reviewAllMode) "All Cards" else "Due Today (${dueCards.size})") },
                            leadingIcon = {
                                Icon(
                                    if (reviewAllMode) Icons.Default.AllInclusive else Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Active Interactive Flashcard
        item {
            if (activeCard != null && activeCardsPool.isNotEmpty()) {
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(350),
                    label = "cardFlip"
                )

                // Current Card Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card ${(cardIndex % activeCardsPool.size) + 1} of ${activeCardsPool.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForgeIndigoLight
                    )
                    Text(
                        text = "Tap card to flip",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                                text = if (!isFlipped) "QUESTION (TAP TO REVEAL)" else "ANSWER & RETENTION",
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
                                text = "Interval: ${activeCard.intervalDays}d • Reps: ${activeCard.repetitions} • Ease: ${String.format("%.1f", activeCard.easeFactor)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFlipped) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                            Text("2 Days", fontSize = 10.sp)
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
                            Text("4 Days", fontSize = 10.sp)
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
                            Text("7 Days", fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Celebration,
                            contentDescription = null,
                            tint = ForgeEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (allCards.isEmpty()) "No flashcards yet" else "All scheduled reviews complete!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (allCards.isEmpty()) "Generate instant cards with AI or create one manually." else "Great job! You have zero cards due for retention right now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (deckFilteredCards.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { reviewAllMode = true }
                                ) {
                                    Text("Study All Anyway")
                                }
                            }
                            Button(
                                onClick = { showAiGeneratorDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                            ) {
                                Text("Generate AI Cards")
                            }
                        }
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
                    text = "Library Cards (${deckFilteredCards.size})",
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Interval: ${card.intervalDays}d • Ease: ${String.format("%.1f", card.easeFactor)}",
                            fontSize = 10.sp,
                            color = ForgeIndigoLight
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
                            onClick = { viewModel.deleteFlashcard(card.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Flashcard",
                                tint = ForgeRose,
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
                        label = { Text("Front (Prompt / Question / Trigger)") },
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
                            viewModel.createCustomFlashcard(
                                deckId = selectedDeckId ?: 1L,
                                front = manualFront.trim(),
                                back = manualBack.trim()
                            )
                            showManualAddDialog = false
                            manualFront = ""
                            manualBack = ""
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
                        label = { Text("Front") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBack,
                        onValueChange = { editBack = it },
                        label = { Text("Back") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingCard?.let { c ->
                            viewModel.editFlashcard(c.copy(front = editFront.trim(), back = editBack.trim()))
                        }
                        editingCard = null
                    }
                ) {
                    Text("Update")
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a topic, chapter, or formula to automatically generate high-yield active recall flashcards:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = generatorTopic,
                        onValueChange = { generatorTopic = it },
                        label = { Text("Topic / Concept") },
                        placeholder = { Text("e.g. Work-Energy Theorem & Power") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Card Count:", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(3, 5, 8).forEach { cnt ->
                                FilterChip(
                                    selected = generatorCount == cnt,
                                    onClick = { generatorCount = cnt },
                                    label = { Text("$cnt") }
                                )
                            }
                        }
                    }

                    if (aiStatusMessage != null) {
                        Text(
                            text = aiStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isAiGenerating) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topic = generatorTopic.trim()
                        if (topic.isNotBlank() && !isAiGenerating) {
                            isAiGenerating = true
                            aiStatusMessage = "Synthesizing flashcards with AI..."
                            viewModel.generateFlashcardsWithAi(
                                topic = topic,
                                deckId = selectedDeckId ?: 1L,
                                count = generatorCount
                            ) { success, msg ->
                                isAiGenerating = false
                                aiStatusMessage = msg
                                if (success) {
                                    showAiGeneratorDialog = false
                                    generatorTopic = ""
                                }
                            }
                        }
                    },
                    enabled = !isAiGenerating && generatorTopic.isNotBlank()
                ) {
                    if (isAiGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generating...")
                    } else {
                        Text("Generate & Save")
                    }
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
