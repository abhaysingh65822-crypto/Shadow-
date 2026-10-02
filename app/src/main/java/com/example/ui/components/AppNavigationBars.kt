package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.theme.*

data class MoreToolItem(
    val route: StudyForgeRoute,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTag: String
)

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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForgeIndigo,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "StudyForge",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentRoute.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            // Combined Streak & Level Compact Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ForgeAmber.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForgeAmber.copy(alpha = 0.35f)),
                modifier = Modifier
                    .clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
                    .padding(end = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = ForgeAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${profile?.currentStreak ?: 0}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ForgeAmber
                    )
                    Text(
                        text = " • ",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "L${profile?.level ?: 1}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = ForgeIndigoLight
                    )
                }
            }

            // Global Search Trigger
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Notification Bell with Badge
            IconButton(
                onClick = { viewModel.navigateTo(StudyForgeRoute.Notifications) },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_notifications_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge { Text("$unreadCount", fontSize = 9.sp) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = if (unreadCount > 0) ForgeAmber else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // AI Copilot trigger
            IconButton(
                onClick = { viewModel.showCopilotDialog.value = true },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_copilot_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Copilot",
                    tint = ForgeIndigoLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyForgeBottomBar(viewModel: StudyForgeViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    var showMoreSheet by remember { mutableStateOf(false) }

    val moreItems = remember {
        listOf(
            MoreToolItem(
                route = StudyForgeRoute.Flashcards,
                title = "Spaced Repetition",
                subtitle = "SM-2 Flashcard retention",
                icon = Icons.Default.Style,
                accentColor = ForgeAmber,
                testTag = "more_item_flashcards"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Planner,
                title = "Smart Planner",
                subtitle = "Task scheduling & AI plan",
                icon = Icons.Default.CalendarToday,
                accentColor = ForgeIndigoLight,
                testTag = "more_item_planner"
            ),
            MoreToolItem(
                route = StudyForgeRoute.MistakeBook,
                title = "Mistake Book",
                subtitle = "Error diagnostics & retry",
                icon = Icons.Default.ErrorOutline,
                accentColor = ForgeRose,
                testTag = "more_item_mistakes"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Notes,
                title = "Study Notes",
                subtitle = "Rich markdown & AI summary",
                icon = Icons.Default.Description,
                accentColor = ForgeCyan,
                testTag = "more_item_notes"
            ),
            MoreToolItem(
                route = StudyForgeRoute.DocumentReader,
                title = "Document Reader",
                subtitle = "PDF viewer & extractor",
                icon = Icons.Default.PictureAsPdf,
                accentColor = ForgeIndigo,
                testTag = "more_item_reader"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Analytics,
                title = "Learning Analytics",
                subtitle = "Real telemetry & charts",
                icon = Icons.Default.BarChart,
                accentColor = ForgeEmerald,
                testTag = "more_item_analytics"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Bookmarks,
                title = "Bookmarks Library",
                subtitle = "Saved questions & items",
                icon = Icons.Default.Bookmark,
                accentColor = ForgeAmber,
                testTag = "more_item_bookmarks"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Profile,
                title = "Student Profile",
                subtitle = "Target exam & milestones",
                icon = Icons.Default.Person,
                accentColor = ForgeViolet,
                testTag = "more_item_profile"
            ),
            MoreToolItem(
                route = StudyForgeRoute.Settings,
                title = "Settings & Config",
                subtitle = "AI models, keys & data",
                icon = Icons.Default.Settings,
                accentColor = Color(0xFF64748B),
                testTag = "more_item_settings"
            )
        )
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Dashboard,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Dashboard) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = {
                Text(
                    text = "Home",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Syllabus,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Syllabus) },
            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Syllabus") },
            label = {
                Text(
                    text = "Syllabus",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.QuestionBank || currentRoute == StudyForgeRoute.TestEngine || currentRoute == StudyForgeRoute.ActiveTest,
            onClick = { viewModel.navigateTo(StudyForgeRoute.QuestionBank) },
            icon = { Icon(Icons.Default.Quiz, contentDescription = "Practice") },
            label = {
                Text(
                    text = "Practice",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.Pomodoro,
            onClick = { viewModel.navigateTo(StudyForgeRoute.Pomodoro) },
            icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
            label = {
                Text(
                    text = "Focus",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = currentRoute == StudyForgeRoute.AiBots,
            onClick = { viewModel.navigateTo(StudyForgeRoute.AiBots) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
            label = {
                Text(
                    text = "AI Bots",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = showMoreSheet || currentRoute in listOf(
                StudyForgeRoute.Notes, StudyForgeRoute.DocumentReader, StudyForgeRoute.Flashcards,
                StudyForgeRoute.Planner, StudyForgeRoute.MistakeBook, StudyForgeRoute.Analytics,
                StudyForgeRoute.Bookmarks, StudyForgeRoute.Profile, StudyForgeRoute.Settings
            ),
            onClick = { showMoreSheet = true },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
            label = {
                Text(
                    text = "More",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            alwaysShowLabel = true,
            modifier = Modifier.testTag("nav_more_btn")
        )
    }

    // Modern ModalBottomSheet for More Menu (responsive, accessible, safe-insets)
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "StudyForge Ecosystem",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Select any specialized study module",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showMoreSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close sheet", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(moreItems) { item ->
                        val isCurrent = currentRoute == item.route
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(94.dp)
                                .testTag(item.testTag)
                                .clickable {
                                    showMoreSheet = false
                                    viewModel.navigateTo(item.route)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) ForgeIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, ForgeIndigoLight) else null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = item.accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = item.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isCurrent) ForgeIndigoLight else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
