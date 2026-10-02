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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiBotType
import com.example.data.NoteEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiBotsScreen(viewModel: StudyForgeViewModel) {
    val selectedBot by viewModel.selectedBot.collectAsStateWithLifecycle()
    val chatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val inputMessage by viewModel.aiChatInput.collectAsStateWithLifecycle()
    val isGenerating by viewModel.aiIsGenerating.collectAsStateWithLifecycle()
    val searchGrounding by viewModel.aiSearchGroundingEnabled.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var savedNotice by remember { mutableStateOf<String?>(null) }

    val currentModel = remember(viewModel.aiManager.getSelectedModel()) {
        viewModel.aiManager.getSelectedModel()
    }

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
                                        AiBotType.AI_QUESTION_GENERATOR -> Icons.AutoMirrored.Filled.HelpOutline
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

        // Model & Search Grounding Controls Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = searchGrounding,
                        onClick = { viewModel.aiSearchGroundingEnabled.value = !searchGrounding },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text("Google Search Grounding", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForgeEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = ForgeEmerald
                        )
                    )
                }

                item {
                    FilterChip(
                        selected = viewModel.aiManager.getSelectedModel() == "gemini-3.5-flash",
                        onClick = { viewModel.aiManager.setSelectedModel("gemini-3.5-flash") },
                        label = { Text("Gemini 3.5 Flash", fontSize = 11.sp) }
                    )
                }

                item {
                    FilterChip(
                        selected = viewModel.aiManager.getSelectedModel() == "gemini-3.1-pro-preview",
                        onClick = { viewModel.aiManager.setSelectedModel("gemini-3.1-pro-preview") },
                        label = { Text("3.1 Pro (Complex Reasoning)", fontSize = 11.sp) }
                    )
                }

                item {
                    FilterChip(
                        selected = viewModel.aiManager.getSelectedModel() == "gemini-3.1-flash-lite-preview",
                        onClick = { viewModel.aiManager.setSelectedModel("gemini-3.1-flash-lite-preview") },
                        label = { Text("3.1 Flash Lite (Fast)", fontSize = 11.sp) }
                    )
                }
            }
        }

        if (savedNotice != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ForgeEmerald.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = savedNotice ?: "",
                        fontSize = 11.sp,
                        color = ForgeEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { savedNotice = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
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
                        modifier = Modifier.widthIn(max = 320.dp),
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
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )

                            if (!isUser && text.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(text))
                                            savedNotice = "Copied to clipboard!"
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.repository.insertNote(
                                                    NoteEntity(
                                                        title = "${selectedBot.title} Insight",
                                                        contentMarkdown = text,
                                                        subjectName = "AI Notes"
                                                    )
                                                )
                                                savedNotice = "Saved to Study Notes!"
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.BookmarkAdd,
                                            contentDescription = "Save to Notes",
                                            modifier = Modifier.size(14.dp),
                                            tint = ForgeAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ForgeIndigoLight,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (searchGrounding) "${selectedBot.title} is researching via Google Search..." else "${selectedBot.title} is synthesizing conceptual answer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Input Field and Send Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { viewModel.aiChatInput.value = it },
                    placeholder = {
                        Text(
                            "Ask ${selectedBot.title}...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForgeIndigoLight,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { viewModel.sendAiChatMessage() },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_ai_button"),
                    containerColor = ForgeIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
