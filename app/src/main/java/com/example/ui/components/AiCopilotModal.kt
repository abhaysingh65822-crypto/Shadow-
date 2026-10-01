package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCopilotModal(viewModel: StudyForgeViewModel) {
    val isVisible by viewModel.showCopilotDialog.collectAsStateWithLifecycle()
    val isLoading by viewModel.copilotIsLoading.collectAsStateWithLifecycle()
    val copilotResponse by viewModel.copilotResponse.collectAsStateWithLifecycle()
    var inputQuery by remember { mutableStateOf("") }

    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.showCopilotDialog.value = false
            viewModel.copilotResponse.value = null
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("copilot_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ForgeIndigoLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "StudyForge AI Copilot & Command Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { viewModel.showCopilotDialog.value = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Type an action like \"Start focus session\", \"Show weak chapters\", \"Revise flashcards\", or ask any academic question.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("What would you like StudyForge to do?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            viewModel.executeCopilotCommand(inputQuery)
                            inputQuery = ""
                        },
                        enabled = inputQuery.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Execute",
                            tint = if (inputQuery.isNotBlank()) ForgeIndigoLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Command Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = { viewModel.executeCopilotCommand("Start a 25 min Focus session") },
                    label = { Text("Start Focus", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.executeCopilotCommand("Show my weak chapters") },
                    label = { Text("Weak Topics", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = { viewModel.executeCopilotCommand("Revise flashcards") },
                    label = { Text("Revise Cards", fontSize = 11.sp) }
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Processing command & query...", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (copilotResponse != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Copilot Response (${copilotResponse!!.modelUsed}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ForgeIndigoLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = copilotResponse!!.text,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
