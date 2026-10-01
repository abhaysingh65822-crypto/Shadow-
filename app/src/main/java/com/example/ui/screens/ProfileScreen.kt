package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.UserProfileEntity
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class AchievementItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val progress: String
)

@Composable
fun ProfileScreen(viewModel: StudyForgeViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val sessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val attempts by viewModel.testAttempts.collectAsStateWithLifecycle()
    val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var editExamName by remember { mutableStateOf("") }
    var editDaysRemaining by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val totalMinutes = remember(sessions) { sessions.sumOf { it.durationMinutes } }
    val totalHours = totalMinutes / 60
    val remMinutes = totalMinutes % 60

    val achievements = listOf(
        AchievementItem(
            title = "First Step to Mastery",
            description = "Complete your first focus study session",
            icon = Icons.Default.Timer,
            isUnlocked = sessions.isNotEmpty(),
            progress = if (sessions.isNotEmpty()) "Unlocked" else "0/1 Session"
        ),
        AchievementItem(
            title = "Consistent Scholar",
            description = "Maintain a 3-day continuous study streak",
            icon = Icons.Default.LocalFireDepartment,
            isUnlocked = (profile?.currentStreak ?: 0) >= 3,
            progress = "${profile?.currentStreak ?: 0}/3 Days"
        ),
        AchievementItem(
            title = "Diagnostic Pioneer",
            description = "Complete your first timed examination benchmark",
            icon = Icons.Default.AssignmentTurnedIn,
            isUnlocked = attempts.isNotEmpty(),
            progress = if (attempts.isNotEmpty()) "Unlocked" else "0/1 Test"
        ),
        AchievementItem(
            title = "Error Crusher",
            description = "Resolve and review at least 3 logged mistakes",
            icon = Icons.Default.CheckCircle,
            isUnlocked = mistakes.count { it.reviewStatus == "RESOLVED" } >= 3,
            progress = "${mistakes.count { it.reviewStatus == "RESOLVED" }}/3 Resolved"
        ),
        AchievementItem(
            title = "Retention Specialist",
            description = "Review 10 flashcards via Spaced Repetition",
            icon = Icons.Default.Style,
            isUnlocked = flashcards.sumOf { it.repetitions } >= 10,
            progress = "${flashcards.sumOf { it.repetitions }}/10 Reviews"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeIndigoLight.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ForgeIndigo,
                        modifier = Modifier.size(76.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Scholar Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Level ${profile?.level ?: 1} Master Explorer • ${profile?.totalXp ?: 0} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ForgeIndigoLight,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalHours}h ${remMinutes}m",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForgeIndigoLight
                            )
                            Text(text = "Total Focus Time", style = MaterialTheme.typography.labelSmall)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${profile?.currentStreak ?: 0} Days",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForgeAmber
                            )
                            Text(text = "Current Streak", style = MaterialTheme.typography.labelSmall)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${profile?.longestStreak ?: 0} Days",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForgeCyan
                            )
                            Text(text = "Longest Streak", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Exam Target Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Primary Exam Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profile?.examTargetName ?: "Engineering Board",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${profile?.examTargetDaysRemaining ?: 30} Days Remaining until exam day",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            editExamName = profile?.examTargetName ?: ""
                            editDaysRemaining = "${profile?.examTargetDaysRemaining ?: 30}"
                            showEditGoalDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = ForgeIndigoLight)
                    }
                }
            }
        }

        // Achievements Section
        item {
            Text(
                text = "Milestones & Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(achievements) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.isUnlocked) ForgeEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (item.isUnlocked) ForgeEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (item.isUnlocked) ForgeEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.isUnlocked) ForgeEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.progress,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isUnlocked) ForgeEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditGoalDialog) {
        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            title = { Text("Update Exam Target") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editExamName,
                        onValueChange = { editExamName = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDaysRemaining,
                        onValueChange = { editDaysRemaining = it },
                        label = { Text("Days Remaining") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = editDaysRemaining.toIntOrNull() ?: 30
                        val currentProf = profile ?: UserProfileEntity()
                        coroutineScope.launch {
                            viewModel.repository.updateProfile(
                                currentProf.copy(
                                    examTargetName = editExamName.trim(),
                                    examTargetDaysRemaining = days
                                )
                            )
                            showEditGoalDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
