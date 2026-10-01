package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiProviderType
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: StudyForgeViewModel) {
    var geminiKey by remember { mutableStateOf(viewModel.aiManager.getGeminiApiKey()) }
    var openAiKey by remember { mutableStateOf(viewModel.aiManager.getOpenAiApiKey()) }
    var selectedProvider by remember { mutableStateOf(viewModel.aiManager.getActiveProvider()) }
    var selectedModel by remember { mutableStateOf(viewModel.aiManager.getSelectedModel()) }

    var isGeminiKeyVisible by remember { mutableStateOf(false) }
    var isOpenAiKeyVisible by remember { mutableStateOf(false) }

    val connectionStatus by viewModel.connectionTestStatus.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val aiLogs by viewModel.aiLogs.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showRestoreDemoConfirm by remember { mutableStateOf(false) }
    var exportJsonString by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: AI Multi-Provider Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Multi-Provider AI Model Manager",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dynamic model routing between Google Gemini and OpenAI with verified fallback resilience",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Provider Switcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Default AI Provider", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AiProviderType.values().forEach { prov ->
                            val isSelected = selectedProvider == prov
                            OutlinedButton(
                                onClick = {
                                    selectedProvider = prov
                                    viewModel.aiManager.setActiveProvider(prov)
                                    // select appropriate default model
                                    val newModel = if (prov == AiProviderType.GEMINI) "gemini-3.5-flash" else "gpt-4o-mini"
                                    selectedModel = newModel
                                    viewModel.aiManager.setSelectedModel(newModel)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = ForgeIndigo.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ForgeIndigoLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = prov.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ForgeIndigoLight else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Model Catalog", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredModels = viewModel.aiManager.availableModels.filter { it.provider == selectedProvider }
                    filteredModels.forEach { modelInfo ->
                        val isCurrent = selectedModel == modelInfo.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrent) ForgeIndigoLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isCurrent,
                                    onClick = {
                                        selectedModel = modelInfo.id
                                        viewModel.aiManager.setSelectedModel(modelInfo.id)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = modelInfo.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (modelInfo.isPreview) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ForgeAmber.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "PREVIEW",
                                                    color = ForgeAmber,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${modelInfo.description} • ${modelInfo.contextWindow}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: API Key Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "API Key Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Gemini Key
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = {
                            geminiKey = it
                            viewModel.aiManager.setGeminiApiKey(it)
                        },
                        label = { Text("Google Gemini API Key") },
                        placeholder = { Text("Auto-configured via BuildConfig or paste custom key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isGeminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isGeminiKeyVisible = !isGeminiKeyVisible }) {
                                Icon(
                                    imageVector = if (isGeminiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // OpenAI Key
                    OutlinedTextField(
                        value = openAiKey,
                        onValueChange = {
                            openAiKey = it
                            viewModel.aiManager.setOpenAiApiKey(it)
                        },
                        label = { Text("OpenAI API Key") },
                        placeholder = { Text("sk-...") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isOpenAiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isOpenAiKeyVisible = !isOpenAiKeyVisible }) {
                                Icon(
                                    imageVector = if (isOpenAiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Connection Button
                    Button(
                        onClick = { viewModel.testAiConnection() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_ai_conn_button"),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying Endpoint...")
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Selected Model Connection")
                        }
                    }

                    if (connectionStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = connectionStatus ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section: Seed Data & Local Persistence
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Data Management & Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export complete learning database or toggle realistic STEM curriculum seed data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRestoreDemoConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Seed Data", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.repository.exportDataAsJson()
                                    exportJsonString = json
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Export JSON", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.resetProgress()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset Progress (Keep Syllabi & Notes)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showClearDataConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeRose)
                    ) {
                        Text("Clear All Data (Clean Slate)", fontSize = 12.sp)
                    }

                    if (exportJsonString != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "JSON Export Generated successfully (${exportJsonString!!.length} bytes):\n${exportJsonString!!.take(200)}...",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: AI Request Telemetry Logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "AI Request Audit Logs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(
                            onClick = {
                                coroutineScope.launch { viewModel.repository.clearAiLogs() }
                            }
                        ) {
                            Text("Clear Logs", fontSize = 11.sp)
                        }
                    }

                    if (aiLogs.isEmpty()) {
                        Text(
                            text = "No AI requests dispatched yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        aiLogs.take(5).forEach { log ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "${log.provider} • ${log.model}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(
                                            text = "${log.status} (${log.latencyMs}ms)",
                                            fontSize = 11.sp,
                                            color = if (log.status == "SUCCESS") ForgeEmerald else ForgeAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "\"${log.promptPreview}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreDemoConfirm = false },
            title = { Text("Restore Demo Data?") },
            text = { Text("This will reload comprehensive STEM syllabus, realistic questions, tests, notes, and flashcard sets.") },
            confirmButton = {
                Button(onClick = {
                    showRestoreDemoConfirm = false
                    viewModel.resetToDemoData()
                }) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDemoConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure? This will remove all local syllabus, notes, flashcards, and test attempts.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataConfirm = false
                        viewModel.clearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeRose)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
