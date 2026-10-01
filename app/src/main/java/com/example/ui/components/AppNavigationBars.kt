package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyForgeTopBar(
    viewModel: StudyForgeViewModel,
    onOpenSearch: () -> Unit
) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForgeIndigo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "StudyForge",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = currentRoute.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Streak counter
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForgeAmber.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = ForgeAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${profile?.currentStreak ?: 0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ForgeAmber
                    )
                }
            }

            // XP Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForgeIndigoLight.copy(alpha = 0.15f),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
            ) {
                Text(
                    text = "Lvl ${profile?.level ?: 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ForgeIndigoLight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Global Search Trigger
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("open_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }

            // Notification Bell with Badge
            IconButton(
                onClick = { viewModel.navigateTo(StudyForgeRoute.Notifications) },
                modifier = Modifier.testTag("open_notifications_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text("$unreadCount") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = if (unreadCount > 0) ForgeAmber else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // AI Copilot trigger
            IconButton(
                onClick = { viewModel.showCopilotDialog.value = true },
                modifier = Modifier.testTag("open_copilot_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Copilot",
                    tint = ForgeIndigoLight
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StudyForgeBottomBar(viewModel: StudyForgeViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    var showMoreMenu by remember { mutableStateOf(false) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Dashboard,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Dashboard) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Syllabus,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Syllabus) },
            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Syllabus") },
            label = { Text("Syllabus", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.QuestionBank || currentRoute == StudyForgeRoute.TestEngine || currentRoute == StudyForgeRoute.ActiveTest,
            onClick = { viewModel.navigateTo(StudyForgeRoute.QuestionBank) },
            icon = { Icon(Icons.Default.Quiz, contentDescription = "Practice") },
            label = { Text("Practice", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Pomodoro,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Pomodoro) },
            icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
            label = { Text("Focus", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.AiBots,
            onClick = { viewModel.navigateTo(StudyForgeRoute.AiBots) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
            label = { Text("AI Bots", fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = showMoreMenu || currentRoute in listOf(
                StudyForgeRoute.Notes, StudyForgeRoute.DocumentReader, StudyForgeRoute.Flashcards,
                StudyForgeRoute.Planner, StudyForgeRoute.MistakeBook, StudyForgeRoute.Analytics,
                StudyForgeRoute.Bookmarks, StudyForgeRoute.Profile, StudyForgeRoute.Settings
            ),
            onClick = { showMoreMenu = true },
            icon = {
                Box {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More")
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Spaced Repetition (Cards)") },
                            leadingIcon = { Icon(Icons.Default.Style, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Flashcards)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Smart Planner") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Planner)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mistake Book") },
                            leadingIcon = { Icon(Icons.Default.ErrorOutline, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.MistakeBook)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Study Notes") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Notes)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Document / PDF Reader") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.DocumentReader)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Learning Analytics") },
                            leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Analytics)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bookmarks Library") },
                            leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Bookmarks)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Student Profile & Milestones") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Profile)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings & API Config") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.navigateTo(StudyForgeRoute.Settings)
                            }
                        )
                    }
                }
            },
            label = { Text("More", fontSize = 11.sp) }
        )
    }
}
