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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PlannerTaskEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PlannerScreen(viewModel: StudyForgeViewModel) {
    val tasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("Physics") }
    var newTaskDuration by remember { mutableStateOf("45") }
    var newTaskType by remember { mutableStateOf("STUDY") }
    var newTaskPriority by remember { mutableStateOf("HIGH") }

    var editingTask by remember { mutableStateOf<PlannerTaskEntity?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editSubject by remember { mutableStateOf("") }
    var editDuration by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf("MEDIUM") }

    var isAiGeneratingPlan by remember { mutableStateOf(false) }
    var planMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("planner_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Planner Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Smart Study Timetable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI-optimized high-yield study blocks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    isAiGeneratingPlan = true
                                    viewModel.generateAiStudyPlan { success, msg ->
                                        isAiGeneratingPlan = false
                                        planMessage = msg
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                enabled = !isAiGeneratingPlan
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAiGeneratingPlan) "Planning..." else "AI Plan", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showAddTaskDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task", fontSize = 12.sp)
                            }
                        }
                    }

                    if (planMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = planMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Tasks (${tasks.count { !it.isCompleted }} Active)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    coroutineScope.launch {
                        viewModel.repository.rescheduleMissedTasks()
                        planMessage = "Tasks re-anchored to active study schedule."
                    }
                }) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-Reschedule", fontSize = 12.sp)
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks scheduled. Tap 'Add Task' or 'AI Plan' to generate your study timetable!")
                    }
                }
            }
        } else {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch {
                                    viewModel.repository.updatePlannerTask(
                                        task.copy(isCompleted = isChecked)
                                    )
                                    if (isChecked) {
                                        viewModel.awardXp(20)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (task.taskType) {
                                        "REVISION" -> ForgeAmber.copy(alpha = 0.15f)
                                        "PRACTICE" -> ForgeIndigoLight.copy(alpha = 0.15f)
                                        "TEST" -> ForgeRose.copy(alpha = 0.15f)
                                        else -> ForgeCyan.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = task.taskType,
                                        color = when (task.taskType) {
                                            "REVISION" -> ForgeAmber
                                            "PRACTICE" -> ForgeIndigoLight
                                            "TEST" -> ForgeRose
                                            else -> ForgeCyan
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${task.subjectName} • ${task.durationMinutes}m • ${task.priority}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    editingTask = task
                                    editTitle = task.title
                                    editSubject = task.subjectName
                                    editDuration = "${task.durationMinutes}"
                                    editPriority = task.priority
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Task",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.repository.deletePlannerTask(task.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Schedule Study Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Description") },
                        placeholder = { Text("e.g. Solve 10 Calculus Integrals") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskSubject,
                        onValueChange = { newTaskSubject = it },
                        label = { Text("Subject") },
                        placeholder = { Text("e.g. Physics, Chemistry, Math") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskDuration,
                        onValueChange = { newTaskDuration = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("STUDY", "REVISION", "PRACTICE").forEach { type ->
                            FilterChip(
                                selected = newTaskType == type,
                                onClick = { newTaskType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val duration = newTaskDuration.toIntOrNull() ?: 45
                        if (newTaskTitle.isNotBlank()) {
                            coroutineScope.launch {
                                viewModel.repository.insertPlannerTask(
                                    PlannerTaskEntity(
                                        title = newTaskTitle.trim(),
                                        taskType = newTaskType,
                                        subjectName = newTaskSubject.trim().ifBlank { "Physics" },
                                        durationMinutes = duration,
                                        priority = newTaskPriority,
                                        scheduledDate = System.currentTimeMillis()
                                    )
                                )
                                showAddTaskDialog = false
                                newTaskTitle = ""
                            }
                        }
                    },
                    enabled = newTaskTitle.isNotBlank()
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Task Dialog
    if (editingTask != null) {
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Edit Study Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HIGH", "MEDIUM", "LOW").forEach { prio ->
                            FilterChip(
                                selected = editPriority == prio,
                                onClick = { editPriority = prio },
                                label = { Text(prio, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingTask?.let { task ->
                            val duration = editDuration.toIntOrNull() ?: task.durationMinutes
                            viewModel.editPlannerTask(
                                task.copy(
                                    title = editTitle.trim(),
                                    subjectName = editSubject.trim(),
                                    durationMinutes = duration,
                                    priority = editPriority
                                )
                            )
                        }
                        editingTask = null
                    },
                    enabled = editTitle.isNotBlank()
                ) {
                    Text("Update Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTask = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
