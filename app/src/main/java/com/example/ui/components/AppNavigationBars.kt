package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand OS Pill & Active Context
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ForgeIndigo, ForgeViolet, ForgeCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(9.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "STUDYFORGE",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.1.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ForgeCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "OS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForgeCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = currentRoute.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = ForgeIndigoLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Quick Status & Action HUD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Streak & Level OS Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForgeAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { viewModel.navigateTo(StudyForgeRoute.Profile) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = ForgeAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${profile?.currentStreak ?: 0}d",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ForgeAmber
                        )
                        Text(
                            text = " • ",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Lv${profile?.level ?: 1}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = ForgeIndigoLight
                        )
                    }
                }

                // Global Search
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("open_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Notifications with live badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(StudyForgeRoute.Notifications) },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("open_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = ForgeRose,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = if (unreadCount > 9) "9+" else "$unreadCount", fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = if (unreadCount > 0) ForgeAmber else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // AI Copilot Quick Floating Capsule
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ForgeIndigo,
                    modifier = Modifier
                        .height(34.dp)
                        .clickable { viewModel.showCopilotDialog.value = true }
                        .testTag("open_copilot_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Copilot",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
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
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
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
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
            )

            NavigationBarItem(
                selected = currentRoute in listOf(StudyForgeRoute.QuestionBank, StudyForgeRoute.TestEngine, StudyForgeRoute.ActiveTest),
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
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
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
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
            )

            NavigationBarItem(
                selected = currentRoute == StudyForgeRoute.AiBots,
                onClick = { viewModel.navigateTo(StudyForgeRoute.AiBots) },
                icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Bots") },
                label = {
                    Text(
                        text = "AI Bots",
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
            )

            NavigationBarItem(
                selected = showMoreSheet,
                onClick = { showMoreSheet = true },
                icon = { Icon(Icons.Default.Apps, contentDescription = "More Tools") },
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForgeIndigoLight,
                    indicatorColor = ForgeIndigo.copy(alpha = 0.18f)
                )
            )
        }
    }

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = CircleShape
                ) {
                    Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STUDY TOOLS & MODULES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForgeIndigoLight,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "StudyForge Operating System",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    IconButton(onClick = { showMoreSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(moreItems) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    showMoreSheet = false
                                    viewModel.navigateTo(item.route)
                                }
                                .testTag(item.testTag),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                item.accentColor.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = item.accentColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = item.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
