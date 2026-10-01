package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

@Composable
fun AiBotsScreen(viewModel: StudyForgeViewModel) {
    val selectedBot by viewModel.selectedBot.collectAsStateWithLifecycle()
    val chatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val inputMessage by viewModel.aiChatInput.collectAsStateWithLifecycle()
    val isGenerating by viewModel.aiIsGenerating.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_bots_screen")
    ) {
        // 10 Bots Selector Carousel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AiBotType.values()) { bot ->
                        val isSelected = selectedBot == bot
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectedBot.value = bot },
                            color = if (isSelected) ForgeIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (bot) {
                                        AiBotType.AI_PHYSICS_TUTOR -> Icons.Default.Bolt
                                        AiBotType.AI_CHEMISTRY_TUTOR -> Icons.Default.Science
                                        AiBotType.AI_MATHEMATICS_SOLVER -> Icons.Default.Functions
                                        AiBotType.AI_CODING_TUTOR -> Icons.Default.Terminal
                                        AiBotType.AI_PLANNER -> Icons.AutoMirrored.Filled.EventNote
                                        AiBotType.AI_EXAMINER -> Icons.Default.Quiz
                                        AiBotType.AI_REVISION_COACH -> Icons.Default.Replay
                                        AiBotType.AI_NOTES_ASSISTANT -> Icons.Default.Description
                                        AiBotType.AI_FLASHCARD_GENERATOR -> Icons.Default.Style
                                        else -> Icons.Default.Psychology
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = bot.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Bot Persona Info Pill
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedBot.title} • ${selectedBot.shortDesc}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ForgeIndigoLight.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = viewModel.aiManager.getSelectedModel(),
                            color = ForgeIndigoLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            viewModel.aiChatMessages.value = listOf(
                                "BOT" to "Cleared chat history. I am ready to guide you through your next topic or problem."
                            )
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Chat Message Log
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { (sender, text) ->
                val isUser = sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Surface(
                            shape = CircleShape,
                            color = ForgeIndigoLight,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        modifier = Modifier.widthIn(max = 300.dp),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) ForgeIndigo else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedBot.title} is synthesizing answer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Pedagogical & Suggestion Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val cognitiveModes = listOf(
                    "💡 Simplify" to "Simplify the explanation and explain like I'm a beginner: ",
                    "🔬 Analogy" to "Give me an intuitive real-world analogy for: ",
                    "📝 Step-by-Step" to "Solve this step-by-step with formulas and derivations: ",
                    "🎯 Quiz Me" to "Ask me a conceptual multiple-choice question on: ",
                    "🔍 Find Mistake" to "Here is my reasoning/solution, please diagnose my misconception: "
                )
                items(cognitiveModes) { (label, prefix) ->
                    SuggestionChip(
                        onClick = {
                            val currentInput = viewModel.aiChatInput.value
                            viewModel.aiChatInput.value = if (currentInput.isNotBlank()) "$prefix$currentInput" else prefix
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = when (selectedBot) {
                    AiBotType.AI_PHYSICS_TUTOR -> listOf("Friction vs Applied Force", "Moment of inertia of sphere", "Work energy theorem proof")
                    AiBotType.AI_CHEMISTRY_TUTOR -> listOf("Le Chatelier equilibrium shifts", "Inert gas effect at constant V", "Exothermic reaction temperature rule")
                    AiBotType.AI_MATHEMATICS_SOLVER -> listOf("Evaluate limit of (sin(3x)-3x)/x^3", "Leibniz rule differentiation", "Integration by parts trick")
                    AiBotType.AI_CODING_TUTOR -> listOf("Time complexity of BST search", "Array lookup vs Hash map", "Two pointer technique")
                    AiBotType.AI_PLANNER -> listOf("Plan my 2-hour study routine", "Reschedule missed tasks", "Prep for upcoming exam")
                    else -> listOf("Explain my weak topics", "Test me on Kinematics", "Create revision sheet")
                }
                items(suggestions) { sugg ->
                    SuggestionChip(
                        onClick = {
                            viewModel.aiChatInput.value = sugg
                            viewModel.sendAiChatMessage()
                        },
                        label = { Text(sugg, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Input Field & Send Action
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { viewModel.aiChatInput.value = it },
                    placeholder = { Text("Ask ${selectedBot.title}...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.sendAiChatMessage() },
                    enabled = inputMessage.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (inputMessage.isNotBlank()) ForgeIndigo else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputMessage.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
