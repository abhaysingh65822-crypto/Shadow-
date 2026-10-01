package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiBotType
import com.example.ai.AiProviderManager
import com.example.ai.AiProviderType
import com.example.ai.AiResponseResult
import com.example.data.ChapterEntity
import com.example.data.FlashcardEntity
import com.example.data.NoteEntity
import com.example.data.NotificationItemEntity
import com.example.data.PlannerTaskEntity
import com.example.data.QuestionEntity
import com.example.data.SeedData
import com.example.data.StudyDocumentEntity
import com.example.data.StudyForgeDatabase
import com.example.data.StudyForgeRepository
import com.example.data.SubjectEntity
import com.example.data.TestEntity
import com.example.data.TopicEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class StudyForgeRoute(val title: String) {
    object Dashboard : StudyForgeRoute("Command Center")
    object Syllabus : StudyForgeRoute("Smart Syllabus")
    object QuestionBank : StudyForgeRoute("Question Bank")
    object TestEngine : StudyForgeRoute("Test Engine")
    object ActiveTest : StudyForgeRoute("Mock Examination")
    object MistakeBook : StudyForgeRoute("Mistake Book")
    object Flashcards : StudyForgeRoute("Spaced Repetition")
    object Planner : StudyForgeRoute("Smart Planner")
    object Pomodoro : StudyForgeRoute("Focus Timer")
    object Notes : StudyForgeRoute("Study Notes")
    object DocumentReader : StudyForgeRoute("Document Reader")
    object AiBots : StudyForgeRoute("AI Specialist Bots")
    object Analytics : StudyForgeRoute("Learning Analytics")
    object Bookmarks : StudyForgeRoute("Bookmarks")
    object Notifications : StudyForgeRoute("Notification Center")
    object Profile : StudyForgeRoute("Student Profile")
    object Settings : StudyForgeRoute("System Settings")
}

data class ActiveTestState(
    val test: TestEntity,
    val questions: List<QuestionEntity>,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(), // question index to selected option
    val markedForReview: Set<Int> = emptySet(),
    val remainingSeconds: Int = 1800,
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val maxScore: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0
)

data class PomodoroState(
    val isRunning: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val currentMode: String = "FOCUS", // FOCUS (25m), SHORT_BREAK (5m), LONG_BREAK (15m)
    val selectedSubjectId: Long = 1,
    val selectedSubjectName: String = "Physics",
    val selectedChapterName: String = "Motion in 1D & Kinematics"
)

class StudyForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StudyForgeDatabase.getDatabase(application, viewModelScope)
    val repository = StudyForgeRepository(database.studyForgeDao())
    val aiManager = AiProviderManager(application, repository)

    // Navigation state
    private val _currentRoute = MutableStateFlow<StudyForgeRoute>(StudyForgeRoute.Dashboard)
    val currentRoute: StateFlow<StudyForgeRoute> = _currentRoute.asStateFlow()

    // Navigation back stack for BackHandler
    private val navStack = mutableListOf<StudyForgeRoute>()

    // Global AI Copilot / Command Bar dialog state
    val showCopilotDialog = MutableStateFlow(false)
    val copilotQuery = MutableStateFlow("")
    val copilotResponse = MutableStateFlow<AiResponseResult?>(null)
    val copilotIsLoading = MutableStateFlow(false)

    // Database reactive flows
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedData.defaultProfile)
    val subjects = repository.allSubjects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val chapters = repository.allChapters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val topics = repository.allTopics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weakTopics = repository.weakTopics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val questions = repository.allQuestions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mistakes = repository.allMistakes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val flashcards = repository.allFlashcards.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dueFlashcards = repository.getDueFlashcards().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val plannerTasks = repository.allPlannerTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val studySessions = repository.allStudySessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tests = repository.allTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val testAttempts = repository.allTestAttempts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val documents = repository.allDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiLogs = repository.aiLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Test Simulator state
    private val _activeTestState = MutableStateFlow<ActiveTestState?>(null)
    val activeTestState: StateFlow<ActiveTestState?> = _activeTestState.asStateFlow()
    private var testTimerJob: Job? = null

    // Pomodoro Timer state
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var pomodoroTimerJob: Job? = null

    // AI Bots State
    val selectedBot = MutableStateFlow(AiBotType.AI_TUTOR)
    val aiChatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "BOT" to "Welcome to StudyForge AI Operating System. I am ready to assist with deep concept mastery, calculations, exam simulations, or personalized study scheduling. What are we mastering today?"
        )
    )
    val aiChatInput = MutableStateFlow("")
    val aiIsGenerating = MutableStateFlow(false)

    // Flashcard review state
    val activeCardIndex = MutableStateFlow(0)
    val isCardFlipped = MutableStateFlow(false)

    // Selected Chapter for drilldown
    val selectedChapter = MutableStateFlow<ChapterEntity?>(null)

    // Selected Document for reading
    val selectedDocument = MutableStateFlow<StudyDocumentEntity?>(null)

    // API settings connection test status
    val connectionTestStatus = MutableStateFlow<String?>(null)
    val isTestingConnection = MutableStateFlow(false)

    fun navigateTo(route: StudyForgeRoute) {
        if (_currentRoute.value != route) {
            navStack.add(_currentRoute.value)
            _currentRoute.value = route
        }
    }

    fun handleBack(): Boolean {
        if (navStack.isNotEmpty()) {
            _currentRoute.value = navStack.removeAt(navStack.size - 1)
            return true
        }
        if (_currentRoute.value != StudyForgeRoute.Dashboard) {
            _currentRoute.value = StudyForgeRoute.Dashboard
            return true
        }
        return false
    }

    // ==========================================
    // Test Engine Operations
    // ==========================================
    fun startTest(test: TestEntity) {
        viewModelScope.launch {
            val allQ = questions.value
            val testQuestions = if (allQ.isNotEmpty()) allQ.shuffled().take(test.totalQuestions) else SeedData.questions
            _activeTestState.value = ActiveTestState(
                test = test,
                questions = testQuestions,
                currentIndex = 0,
                selectedAnswers = emptyMap(),
                markedForReview = emptySet(),
                remainingSeconds = test.durationMinutes * 60,
                isSubmitted = false
            )
            navigateTo(StudyForgeRoute.ActiveTest)
            startTestTimer()
        }
    }

    private fun startTestTimer() {
        testTimerJob?.cancel()
        testTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeTestState.value ?: break
                if (current.isSubmitted) break
                if (current.remainingSeconds <= 1) {
                    submitActiveTest()
                    break
                }
                _activeTestState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
            }
        }
    }

    fun selectTestAnswer(questionIndex: Int, answer: String) {
        val current = _activeTestState.value ?: return
        val updated = current.selectedAnswers.toMutableMap()
        updated[questionIndex] = answer
        _activeTestState.value = current.copy(selectedAnswers = updated)
    }

    fun toggleMarkForReview(questionIndex: Int) {
        val current = _activeTestState.value ?: return
        val updated = current.markedForReview.toMutableSet()
        if (updated.contains(questionIndex)) updated.remove(questionIndex) else updated.add(questionIndex)
        _activeTestState.value = current.copy(markedForReview = updated)
    }

    fun setTestQuestionIndex(index: Int) {
        val current = _activeTestState.value ?: return
        if (index in 0 until current.questions.size) {
            _activeTestState.value = current.copy(currentIndex = index)
        }
    }

    fun submitActiveTest() {
        testTimerJob?.cancel()
        val current = _activeTestState.value ?: return
        if (current.isSubmitted) return

        var score = 0
        var correct = 0
        var wrong = 0
        var skipped = 0
        val wrongList = mutableListOf<Pair<QuestionEntity, String>>()

        for (i in current.questions.indices) {
            val q = current.questions[i]
            val selected = current.selectedAnswers[i]
            if (selected == null) {
                skipped++
            } else if (selected.trim().equals(q.correctAnswer.trim(), ignoreCase = true)) {
                correct++
                score += q.marks
            } else {
                wrong++
                score -= q.negativeMarks.toInt()
                wrongList.add(q to selected)
            }
        }

        val maxScore = current.questions.sumOf { it.marks }
        val durationSpent = (current.test.durationMinutes * 60) - current.remainingSeconds

        _activeTestState.value = current.copy(
            isSubmitted = true,
            score = score,
            maxScore = maxScore,
            correctCount = correct,
            wrongCount = wrong,
            skippedCount = skipped
        )

        viewModelScope.launch {
            repository.recordTestCompletion(
                testId = current.test.id,
                testTitle = current.test.title,
                score = score,
                maxScore = maxScore,
                durationSec = durationSpent,
                attemptedCount = correct + wrong,
                skippedCount = skipped,
                incorrectCount = wrong,
                wrongQuestions = wrongList
            )
        }
    }

    // ==========================================
    // Question Bank Practice
    // ==========================================
    fun attemptQuestionInBank(question: QuestionEntity, chosenOption: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isCorrect = repository.recordQuestionAttempt(question, chosenOption, 35)
            onResult(isCorrect)
        }
    }

    // ==========================================
    // Pomodoro Timer Operations
    // ==========================================
    fun setPomodoroMode(mode: String) {
        val seconds = when (mode) {
            "SHORT_BREAK" -> 5 * 60
            "LONG_BREAK" -> 15 * 60
            else -> 25 * 60
        }
        _pomodoroState.value = _pomodoroState.value.copy(
            currentMode = mode,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false
        )
        pomodoroTimerJob?.cancel()
    }

    fun togglePomodoro() {
        val current = _pomodoroState.value
        if (current.isRunning) {
            pomodoroTimerJob?.cancel()
            _pomodoroState.value = current.copy(isRunning = false)
        } else {
            _pomodoroState.value = current.copy(isRunning = true)
            startPomodoroLoop()
        }
    }

    fun resetPomodoro() {
        pomodoroTimerJob?.cancel()
        val total = _pomodoroState.value.totalSeconds
        _pomodoroState.value = _pomodoroState.value.copy(
            remainingSeconds = total,
            isRunning = false
        )
    }

    private fun startPomodoroLoop() {
        pomodoroTimerJob?.cancel()
        pomodoroTimerJob = viewModelScope.launch {
            while (_pomodoroState.value.isRunning && _pomodoroState.value.remainingSeconds > 0) {
                delay(1000)
                val newRemain = _pomodoroState.value.remainingSeconds - 1
                _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = newRemain)
                if (newRemain == 0) {
                    completePomodoroSession()
                    break
                }
            }
        }
    }

    private fun completePomodoroSession() {
        val current = _pomodoroState.value
        _pomodoroState.value = current.copy(isRunning = false)
        val minutes = current.totalSeconds / 60
        viewModelScope.launch {
            repository.recordStudySession(
                subjectId = current.selectedSubjectId,
                chapterId = null,
                subjectName = current.selectedSubjectName,
                chapterName = current.selectedChapterName,
                durationMinutes = minutes,
                sessionType = current.currentMode,
                notes = "Completed ${current.currentMode} focus block on ${current.selectedChapterName}"
            )
        }
    }

    // ==========================================
    // Spaced Repetition (SM-2) Flashcard Review
    // ==========================================
    fun rateFlashcard(card: FlashcardEntity, rating: Int) {
        viewModelScope.launch {
            repository.reviewFlashcard(card, rating)
            isCardFlipped.value = false
            val currentDue = dueFlashcards.value
            if (activeCardIndex.value < currentDue.size - 1) {
                activeCardIndex.value += 1
            } else {
                activeCardIndex.value = 0
            }
        }
    }

    // ==========================================
    // AI Bots & Chat
    // ==========================================
    fun sendAiChatMessage() {
        val text = aiChatInput.value.trim()
        if (text.isBlank() || aiIsGenerating.value) return

        aiChatInput.value = ""
        val history = aiChatMessages.value.toMutableList()
        history.add("USER" to text)
        aiChatMessages.value = history
        aiIsGenerating.value = true

        viewModelScope.launch {
            val prof = userProfile.value
            val weak = weakTopics.value.joinToString { it.title }
            val contextSummary = "Exam: ${prof?.examTargetName} (${prof?.examTargetDaysRemaining} days remaining). Weak areas: $weak. Active streak: ${prof?.currentStreak} days."

            val result = aiManager.generateResponse(selectedBot.value, text, contextSummary)

            val updatedHistory = aiChatMessages.value.toMutableList()
            updatedHistory.add("BOT" to result.text)
            aiChatMessages.value = updatedHistory
            aiIsGenerating.value = false
        }
    }

    // ==========================================
    // Global Copilot & Command Center Router
    // ==========================================
    fun executeCopilotCommand(query: String) {
        val q = query.trim()
        if (q.isBlank()) return

        copilotIsLoading.value = true
        copilotQuery.value = q

        // Check if query is an actionable navigation command
        val lower = q.lowercase()
        when {
            lower.contains("start") && lower.contains("session") || lower.contains("pomodoro") -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.Pomodoro)
                togglePomodoro()
                copilotIsLoading.value = false
                return
            }
            lower.contains("weak") && (lower.contains("chapter") || lower.contains("topic")) -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.Syllabus)
                copilotIsLoading.value = false
                return
            }
            lower.contains("mistake") -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.MistakeBook)
                copilotIsLoading.value = false
                return
            }
            lower.contains("flashcard") || lower.contains("revise") && lower.contains("card") -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.Flashcards)
                copilotIsLoading.value = false
                return
            }
            lower.contains("plan") || lower.contains("timetable") -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.Planner)
                copilotIsLoading.value = false
                return
            }
            lower.contains("test") -> {
                showCopilotDialog.value = false
                navigateTo(StudyForgeRoute.TestEngine)
                copilotIsLoading.value = false
                return
            }
        }

        // Otherwise invoke AI Copilot intelligence
        viewModelScope.launch {
            val prof = userProfile.value
            val weak = weakTopics.value.joinToString { it.title }
            val context = "Exam target: ${prof?.examTargetName}. Weak areas: $weak. Daily Target: ${prof?.dailyTargetMinutes} mins."
            val res = aiManager.generateResponse(AiBotType.AI_TUTOR, q, context)
            copilotResponse.value = res
            copilotIsLoading.value = false
        }
    }

    // Connection testing
    fun testAiConnection() {
        isTestingConnection.value = true
        connectionTestStatus.value = "Testing ${aiManager.getActiveProvider().displayName} connection..."
        viewModelScope.launch {
            val (ok, msg) = aiManager.testConnection(
                aiManager.getActiveProvider(),
                aiManager.getSelectedModel()
            )
            connectionTestStatus.value = if (ok) "● $msg" else "✕ $msg"
            isTestingConnection.value = false
        }
    }

    // Seed Data & Persistence management
    fun resetToDemoData() {
        viewModelScope.launch {
            repository.restoreSeedData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
