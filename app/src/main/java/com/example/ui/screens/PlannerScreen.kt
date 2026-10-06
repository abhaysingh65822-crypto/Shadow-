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
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("ALL") } // ALL, TODAY, TOMORROW, HIGH_PRIORITY
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Physics") }
    var newTaskDuration by remember { mutableStateOf("45") }
    var newTaskType by remember { mutableStateOf("STUDY") }
    var newTaskPriority by remember { mutableStateOf("HIGH") }
    var newTaskDayOffset by remember { mutableIntStateOf(0) } // 0=Today, 1=Tomorrow, 2=In 2 Days

    var editingTask by remember { mutableStateOf<PlannerTaskEntity?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editSubject by remember { mutableStateOf("") }
    var editDuration by remember { mutableStateOf("") }
    var editPriority by remember { mutableStateOf("MEDIUM") }

    var isAiGeneratingPlan by remember { mutableStateOf(false) }
    var planMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val filteredTasks = remember(tasks, selectedTab) {
        val now = System.currentTimeMillis()
        when (selectedTab) {
            "TODAY" -> tasks.filter { Math.abs(it.scheduledDate - now) < 86400000L }
            "TOMORROW" -> tasks.filter { it.scheduledDate - now in 86400000L..172800000L }
            "HIGH_PRIORITY" -> tasks.filter { it.priority.equals("HIGH", ignoreCase = true) }
            else -> tasks
        }
    }

    val totalPlannedMinutes = remember(filteredTasks) {
        filteredTasks.filter { !it.isCompleted }.sumOf { it.durationMinutes }
    }

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
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.35f))
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
                                text = "Target: ${profile?.examTargetName ?: "General Prep"} • ${totalPlannedMinutes / 60}h ${totalPlannedMinutes % 60}m planned",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    isAiGeneratingPlan = true
                                    planMessage = "Synthesizing customized timetable with AI..."
                                    viewModel.generateAiStudyPlan { success, msg ->
                                        isAiGeneratingPlan = false
                                        planMessage = msg
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                enabled = !isAiGeneratingPlan
                            ) {
                                if (isAiGeneratingPlan) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Planning...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Timetable", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    newTaskTitle = ""
                                    showAddTaskDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Task", fontSize = 12.sp)
                            }
                        }
                    }

                    if (planMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ForgeEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = planMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeEmerald,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Timetable View Tabs
        item {
            val tabsList = listOf(
                "ALL" to "All Tasks (${tasks.size})",
                "TODAY" to "Today",
                "TOMORROW" to "Tomorrow",
                "HIGH_PRIORITY" to "High Priority"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tabsList) { (tabId, tabLabel) ->
                    FilterChip(
                        selected = selectedTab == tabId,
                        onClick = { selectedTab = tabId },
                        label = { Text(tabLabel) },
                        shape = RoundedCornerShape(8.dp)
                    )
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
                    text = "${filteredTasks.count { !it.isCompleted }} Active • ${filteredTasks.count { it.isCompleted }} Done",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    coroutineScope.launch {
                        viewModel.repository.rescheduleMissedTasks()
                        planMessage = "Overdue tasks automatically re-anchored to today."
                    }
                }) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-Reschedule", fontSize = 12.sp)
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
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
                            Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = ForgeIndigoLight,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No study tasks in this view",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'AI Timetable' to generate a targeted study plan or create a custom task.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTasks) { task ->
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
                            onCheckedChange = { viewModel.toggleTaskCompleted(task) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (task.taskType) {
                                        "REVISION" -> ForgeAmber.copy(alpha = 0.15f)
                                        "PRACTICE" -> ForgeIndigoLight.copy(alpha = 0.15f)
                                        "MOCK_TEST" -> ForgeRose.copy(alpha = 0.15f)
                                        else -> ForgeCyan.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = task.taskType,
                                        color = when (task.taskType) {
                                            "REVISION" -> ForgeAmber
                                            "PRACTICE" -> ForgeIndigoLight
                                            "MOCK_TEST" -> ForgeRose
                                            else -> ForgeCyan
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${task.subjectName} • ${task.durationMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (task.priority == "HIGH") {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ForgeRose.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "HIGH",
                                            color = ForgeRose,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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
                                onClick = { viewModel.deletePlannerTask(task.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = ForgeRose,
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
                        placeholder = { Text("e.g. Solve 15 Kinematics Questions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskSubject,
                        onValueChange = { newTaskSubject = it },
                        label = { Text("Subject") },
                        placeholder = { Text("e.g. Physics, Math, Chemistry") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskDuration,
                        onValueChange = { newTaskDuration = it },
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Schedule When:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Today", 1 to "Tomorrow", 2 to "In 2 Days").forEach { (offset, lbl) ->
                            FilterChip(
                                selected = newTaskDayOffset == offset,
                                onClick = { newTaskDayOffset = offset },
                                label = { Text(lbl) }
                            )
                        }
                    }

                    Text("Task Type & Priority:", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("STUDY", "REVISION", "PRACTICE").forEach { type ->
                            FilterChip(
                                selected = newTaskType == type,
                                onClick = { newTaskType = type },
                                label = { Text(type, fontSize = 10.sp) }
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
                            viewModel.addPlannerTask(
                                title = newTaskTitle,
                                subjectName = newTaskSubject,
                                durationMinutes = duration,
                                taskType = newTaskType,
                                priority = newTaskPriority,
                                daysOffset = newTaskDayOffset
                            )
                            showAddTaskDialog = false
                            newTaskTitle = ""
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
