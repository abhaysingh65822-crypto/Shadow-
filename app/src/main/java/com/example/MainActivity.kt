package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.StudyForgeRoute
import com.example.ui.StudyForgeViewModel
import com.example.ui.components.AiCopilotModal
import com.example.ui.components.GlobalSearchModal
import com.example.ui.components.StudyForgeBottomBar
import com.example.ui.components.StudyForgeTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: StudyForgeViewModel = viewModel()
                val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
                var showSearchModal by remember { mutableStateOf(false) }

                BackHandler {
                    viewModel.handleBack()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentRoute != StudyForgeRoute.ActiveTest) {
                            StudyForgeTopBar(
                                viewModel = viewModel,
                                onOpenSearch = { showSearchModal = true }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentRoute != StudyForgeRoute.ActiveTest) {
                            StudyForgeBottomBar(viewModel = viewModel)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentRoute) {
                            StudyForgeRoute.Dashboard -> DashboardScreen(viewModel = viewModel)
                            StudyForgeRoute.Syllabus -> SyllabusScreen(viewModel = viewModel)
                            StudyForgeRoute.QuestionBank -> QuestionBankScreen(viewModel = viewModel)
                            StudyForgeRoute.TestEngine -> TestEngineScreen(viewModel = viewModel)
                            StudyForgeRoute.ActiveTest -> ActiveTestScreen(viewModel = viewModel)
                            StudyForgeRoute.MistakeBook -> MistakeBookScreen(viewModel = viewModel)
                            StudyForgeRoute.Flashcards -> FlashcardsScreen(viewModel = viewModel)
                            StudyForgeRoute.Planner -> PlannerScreen(viewModel = viewModel)
                            StudyForgeRoute.Pomodoro -> PomodoroScreen(viewModel = viewModel)
                            StudyForgeRoute.Notes -> NotesScreen(viewModel = viewModel)
                            StudyForgeRoute.DocumentReader -> DocumentReaderScreen(viewModel = viewModel)
                            StudyForgeRoute.AiBots -> AiBotsScreen(viewModel = viewModel)
                            StudyForgeRoute.Analytics -> AnalyticsScreen(viewModel = viewModel)
                            StudyForgeRoute.Bookmarks -> BookmarksScreen(viewModel = viewModel)
                            StudyForgeRoute.Notifications -> NotificationsScreen(viewModel = viewModel)
                            StudyForgeRoute.Profile -> ProfileScreen(viewModel = viewModel)
                            StudyForgeRoute.Settings -> SettingsScreen(viewModel = viewModel)
                        }

                        // Global AI Copilot Bottom Sheet / Command Bar
                        AiCopilotModal(viewModel = viewModel)

                        // Global Search Modal
                        GlobalSearchModal(
                            viewModel = viewModel,
                            isVisible = showSearchModal,
                            onDismiss = { showSearchModal = false }
                        )
                    }
                }
            }
        }
    }
}
