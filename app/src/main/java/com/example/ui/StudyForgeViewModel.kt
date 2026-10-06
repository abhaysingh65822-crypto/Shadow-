package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiBotType
import com.example.ai.AiProviderManager
import com.example.ai.AiProviderType
import com.example.ai.AiResponseResult
import com.example.data.BookmarkEntity
import com.example.data.ChapterEntity
import com.example.data.DeckEntity
import com.example.data.FlashcardEntity
import com.example.data.MistakeEntity
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
    val targetEndTimeMillis: Long? = null,
    val pausedRemainingSeconds: Int = 25 * 60,
    val currentMode: String = "FOCUS", // FOCUS (25m), SHORT_BREAK (5m), LONG_BREAK (15m)
    val selectedSubjectId: Long = 1,
    val selectedSubjectName: String = "Physics",
    val selectedChapterName: String = "Motion in 1D & Kinematics",
    val sessionNotes: String = ""
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
    val decks = repository.allDecks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    val aiSearchGroundingEnabled = MutableStateFlow(false)

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
    fun startTest(test: TestEntity, customQuestions: List<QuestionEntity>? = null) {
        viewModelScope.launch {
            val allQ = questions.value
            val testQuestions = customQuestions ?: if (allQ.isNotEmpty()) allQ.shuffled().take(test.totalQuestions) else SeedData.questions
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
    // Pomodoro Timer Operations (Monotonic Timestamp)
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
            pausedRemainingSeconds = seconds,
            targetEndTimeMillis = null,
            isRunning = false
        )
        pomodoroTimerJob?.cancel()
    }

    fun togglePomodoro() {
        val current = _pomodoroState.value
        if (current.isRunning) {
            // Pause
            pomodoroTimerJob?.cancel()
            val rem = current.targetEndTimeMillis?.let {
                val diff = ((it - System.currentTimeMillis()) / 1000).toInt()
                diff.coerceAtLeast(0)
            } ?: current.remainingSeconds

            _pomodoroState.value = current.copy(
                isRunning = false,
                targetEndTimeMillis = null,
                remainingSeconds = rem,
                pausedRemainingSeconds = rem
            )
        } else {
            // Start or Resume
            val rem = if (current.remainingSeconds > 0) current.remainingSeconds else current.totalSeconds
            val targetEnd = System.currentTimeMillis() + (rem * 1000L)
            _pomodoroState.value = current.copy(
                isRunning = true,
                remainingSeconds = rem,
                targetEndTimeMillis = targetEnd
            )
            startPomodoroLoop()
        }
    }

    fun resetPomodoro() {
        pomodoroTimerJob?.cancel()
        val total = _pomodoroState.value.totalSeconds
        _pomodoroState.value = _pomodoroState.value.copy(
            remainingSeconds = total,
            pausedRemainingSeconds = total,
            targetEndTimeMillis = null,
            isRunning = false
        )
    }

    fun skipPomodoro() {
        pomodoroTimerJob?.cancel()
        val next = when (_pomodoroState.value.currentMode) {
            "FOCUS" -> "SHORT_BREAK"
            "SHORT_BREAK" -> "FOCUS"
            else -> "FOCUS"
        }
        setPomodoroMode(next)
    }

    fun endPomodoroEarly() {
        val current = _pomodoroState.value
        val elapsedSec = current.totalSeconds - current.remainingSeconds
        val elapsedMin = elapsedSec / 60
        resetPomodoro()
        if (elapsedMin >= 1) {
            viewModelScope.launch {
                repository.recordStudySession(
                    subjectId = current.selectedSubjectId,
                    chapterId = null,
                    subjectName = current.selectedSubjectName,
                    chapterName = current.selectedChapterName,
                    durationMinutes = elapsedMin,
                    sessionType = current.currentMode,
                    notes = current.sessionNotes.ifBlank { "Study session on ${current.selectedSubjectName}" }
                )
            }
        }
    }

    fun setPomodoroSubject(subjectId: Long, subjectName: String) {
        _pomodoroState.value = _pomodoroState.value.copy(
            selectedSubjectId = subjectId,
            selectedSubjectName = subjectName
        )
    }

    fun setPomodoroNotes(notes: String) {
        _pomodoroState.value = _pomodoroState.value.copy(sessionNotes = notes)
    }

    private fun startPomodoroLoop() {
        pomodoroTimerJob?.cancel()
        pomodoroTimerJob = viewModelScope.launch {
            while (_pomodoroState.value.isRunning) {
                delay(500)
                val current = _pomodoroState.value
                if (!current.isRunning || current.targetEndTimeMillis == null) break
                val now = System.currentTimeMillis()
                val diffSec = ((current.targetEndTimeMillis - now) / 1000).toInt()
                if (diffSec <= 0) {
                    _pomodoroState.value = current.copy(
                        remainingSeconds = 0,
                        isRunning = false,
                        targetEndTimeMillis = null
                    )
                    completePomodoroSession()
                    break
                } else {
                    _pomodoroState.value = current.copy(remainingSeconds = diffSec)
                }
            }
        }
    }

    private fun completePomodoroSession() {
        val current = _pomodoroState.value
        _pomodoroState.value = current.copy(isRunning = false, remainingSeconds = 0)
        val minutes = current.totalSeconds / 60
        viewModelScope.launch {
            repository.recordStudySession(
                subjectId = current.selectedSubjectId,
                chapterId = null,
                subjectName = current.selectedSubjectName,
                chapterName = current.selectedChapterName,
                durationMinutes = minutes,
                sessionType = current.currentMode,
                notes = current.sessionNotes.ifBlank { "Completed ${current.currentMode} focus block on ${current.selectedSubjectName}" }
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

            val pastHistory = history.dropLast(1)
            val result = aiManager.generateResponse(
                botType = selectedBot.value,
                userPrompt = text,
                contextSummary = contextSummary,
                history = pastHistory,
                enableSearchGrounding = aiSearchGroundingEnabled.value
            )

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

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData()
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // ==========================================
    // Syllabus CRUD Operations
    // ==========================================
    fun addSubject(name: String, code: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    name = name.trim(),
                    code = code.trim().ifBlank { "SUB-${System.currentTimeMillis() % 1000}" },
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun editSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subjectId: Long) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
        }
    }

    fun addChapter(subjectId: Long, title: String) {
        viewModelScope.launch {
            val currentCount = chapters.value.count { it.subjectId == subjectId }
            repository.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    title = title.trim(),
                    orderIndex = currentCount + 1,
                    completionPercent = 0,
                    masteryLevel = 0,
                    confidenceScore = 0,
                    timeSpentMinutes = 0
                )
            )
        }
    }

    fun editChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter)
        }
    }

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch {
            repository.deleteChapter(chapterId)
        }
    }

    fun addTopic(chapterId: Long, title: String, difficulty: String = "MEDIUM") {
        viewModelScope.launch {
            repository.insertTopic(
                TopicEntity(
                    chapterId = chapterId,
                    title = title.trim(),
                    difficultyLevel = difficulty,
                    isCompleted = false,
                    masteryScore = 0,
                    accuracyRate = 0,
                    isWeak = false
                )
            )
        }
    }

    fun editTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic)
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            repository.deleteTopic(topicId)
        }
    }

    fun toggleTopicCompleted(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(isCompleted = !topic.isCompleted))
        }
    }

    // AI Syllabus Builder
    fun createSyllabusWithAi(subjectName: String, targetExam: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val subId = repository.insertSubject(
                    SubjectEntity(
                        name = subjectName.trim(),
                        code = "GEN-${System.currentTimeMillis() % 1000}",
                        colorHex = "#4F46E5",
                        iconName = "menu_book"
                    )
                )
                val (chaps, tops) = aiManager.generateStructuredSyllabus(subjectName, targetExam)
                chaps.forEach { chap ->
                    val chapId = repository.insertChapter(chap.copy(subjectId = subId))
                    tops.take(3).forEach { top ->
                        repository.insertTopic(top.copy(chapterId = chapId))
                    }
                }
                onDone(true, "Generated ${chaps.size} chapters for $subjectName!")
            } catch (e: Exception) {
                onDone(false, e.localizedMessage ?: "Generation failed")
            }
        }
    }

    // ==========================================
    // Question Bank CRUD & AI Question Generator
    // ==========================================
    fun generateQuestionsWithAi(
        subjectId: Long,
        chapterId: Long,
        topicId: Long,
        subject: String,
        topic: String,
        difficulty: String,
        type: String,
        count: Int,
        onDone: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val generated = aiManager.generateStructuredQuestions(
                    subjectId = subjectId,
                    chapterId = chapterId,
                    topicId = topicId,
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    questionType = type,
                    count = count
                )
                generated.forEach { q ->
                    repository.insertQuestion(q)
                }
                onDone(true, "Successfully generated and saved ${generated.size} questions!")
            } catch (e: Exception) {
                onDone(false, e.localizedMessage ?: "Failed to generate questions")
            }
        }
    }

    fun addCustomQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.insertQuestion(question)
        }
    }

    fun deleteQuestion(questionId: Long) {
        viewModelScope.launch {
            repository.deleteQuestion(questionId)
        }
    }

    // ==========================================
    // Test Engine CRUD
    // ==========================================
    fun createCustomTest(
        title: String,
        subjectFilter: String,
        testType: String,
        durationMinutes: Int,
        totalMarks: Int,
        totalQuestions: Int
    ) {
        viewModelScope.launch {
            repository.insertTest(
                TestEntity(
                    title = title.trim(),
                    testType = testType,
                    durationMinutes = durationMinutes,
                    totalMarks = totalMarks,
                    totalQuestions = totalQuestions,
                    subjectFilter = subjectFilter
                )
            )
        }
    }

    fun deleteTest(testId: Long) {
        viewModelScope.launch {
            repository.deleteTest(testId)
        }
    }

    // ==========================================
    // Profile & Settings
    // ==========================================
    fun updateUserProfile(
        name: String,
        targetExam: String,
        examDateMillis: Long?,
        targetMinutes: Int,
        difficulty: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: SeedData.defaultProfile
            val daysRem = if (examDateMillis != null && examDateMillis > System.currentTimeMillis()) {
                ((examDateMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
            } else {
                0
            }
            repository.updateProfile(
                current.copy(
                    name = name.trim().ifBlank { current.name },
                    examTargetName = targetExam.trim(),
                    examDateMillis = examDateMillis,
                    examTargetDaysRemaining = daysRem,
                    dailyTargetMinutes = targetMinutes.coerceIn(15, 600),
                    difficultyPreference = difficulty
                )
            )
        }
    }

    // ==========================================
    // Bookmarks Management
    // ==========================================
    fun toggleBookmark(type: String, itemId: Long, title: String, subtitle: String = "", route: String = "") {
        viewModelScope.launch {
            val isBookmarked = bookmarks.value.any { it.itemType.equals(type, ignoreCase = true) && it.itemId == itemId }
            repository.toggleBookmark(type, itemId, title, subtitle, route, isBookmarked)
        }
    }

    // ==========================================
    // Flashcard Decks & Editing
    // ==========================================
    fun createDeck(name: String, subjectId: Long, description: String = "") {
        viewModelScope.launch {
            repository.insertDeck(DeckEntity(name = name.trim(), subjectId = subjectId, description = description.trim()))
        }
    }

    fun deleteDeck(deckId: Long) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }

    fun editFlashcard(flashcard: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(flashcard)
        }
    }

    fun deleteFlashcard(cardId: Long) {
        viewModelScope.launch {
            repository.deleteFlashcard(cardId)
        }
    }

    fun createCustomFlashcard(deckId: Long, front: String, back: String, cardType: String = "STANDARD") {
        viewModelScope.launch {
            repository.insertFlashcard(
                FlashcardEntity(
                    deckId = deckId,
                    front = front.trim(),
                    back = back.trim(),
                    cardType = cardType,
                    repetitions = 0,
                    intervalDays = 1,
                    easeFactor = 2.5f,
                    dueDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun generateFlashcardsWithAi(topic: String, deckId: Long, count: Int = 3, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val generated = aiManager.generateStructuredFlashcards(
                    topic = topic.trim(),
                    count = count,
                    deckId = deckId
                )
                generated.forEach { card ->
                    repository.insertFlashcard(card)
                }
                onDone(true, "Successfully generated and saved ${generated.size} active-recall flashcards!")
            } catch (e: Exception) {
                onDone(false, e.localizedMessage ?: "Failed to generate flashcards")
            }
        }
    }

    // ==========================================
    // Notes Editing
    // ==========================================
    fun editNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    // ==========================================
    // Study Documents CRUD
    // ==========================================
    fun addDocument(title: String, summary: String, content: String, pageCount: Int = 1, fileType: String = "PDF") {
        viewModelScope.launch {
            repository.insertDocument(
                StudyDocumentEntity(
                    title = title.trim(),
                    summary = summary.trim(),
                    contentExtract = content.trim(),
                    pageCount = pageCount.coerceAtLeast(1),
                    fileType = fileType
                )
            )
        }
    }

    fun deleteDocument(docId: Long) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
            if (selectedDocument.value?.id == docId) {
                selectedDocument.value = null
            }
        }
    }

    // ==========================================
    // Planner Tasks Editing & AI Plan Generator
    // ==========================================
    fun editPlannerTask(task: PlannerTaskEntity) {
        viewModelScope.launch {
            repository.updatePlannerTask(task)
        }
    }

    fun addPlannerTask(
        title: String,
        subjectName: String,
        durationMinutes: Int,
        taskType: String = "STUDY",
        priority: String = "HIGH",
        daysOffset: Int = 0
    ) {
        viewModelScope.launch {
            val scheduled = System.currentTimeMillis() + (daysOffset * 86400000L)
            repository.insertPlannerTask(
                PlannerTaskEntity(
                    title = title.trim(),
                    subjectName = subjectName.trim().ifBlank { "General" },
                    chapterName = "Active Chapter",
                    durationMinutes = durationMinutes.coerceIn(15, 240),
                    taskType = taskType,
                    priority = priority,
                    scheduledDate = scheduled,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTaskCompleted(task: PlannerTaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updatePlannerTask(updated)
            if (updated.isCompleted) {
                awardXp(15)
            }
        }
    }

    fun deletePlannerTask(taskId: Long) {
        viewModelScope.launch {
            repository.deletePlannerTask(taskId)
        }
    }

    fun generateAiStudyPlan(onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val userTarget = userProfile.value?.examTargetName?.ifBlank { "General STEM Studies" } ?: "General STEM Studies"
                val allSubs = subjects.value.map { it.name }
                val weak = weakTopics.value.map { it.title }

                val generatedTasks = aiManager.generateStructuredStudyPlan(
                    targetExam = userTarget,
                    subjectsList = allSubs,
                    weakTopicsList = weak
                )

                generatedTasks.forEach { task ->
                    repository.insertPlannerTask(task)
                }
                onDone(true, "AI generated and added ${generatedTasks.size} structured study sessions for $userTarget!")
            } catch (e: Exception) {
                onDone(false, e.localizedMessage ?: "Failed to generate study plan")
            }
        }
    }

    // ==========================================
    // Mistake Editing & Retry
    // ==========================================
    fun editMistake(mistake: MistakeEntity) {
        viewModelScope.launch {
            repository.updateMistake(mistake)
        }
    }

    fun awardXp(amount: Int) {
        viewModelScope.launch {
            repository.awardXp(amount)
        }
    }

    fun retryMistake(mistake: MistakeEntity) {
        val q = questions.value.find { it.id == mistake.questionId }
        if (q != null) {
            startTest(
                test = TestEntity(
                    id = 0,
                    title = "Mistake Retry: ${mistake.subjectName}",
                    testType = "PRACTICE",
                    durationMinutes = 10,
                    totalMarks = 4,
                    totalQuestions = 1
                ),
                customQuestions = listOf(q)
            )
        } else {
            navigateTo(StudyForgeRoute.QuestionBank)
        }
    }

    fun setPomodoroFocusDuration(minutes: Int) {
        val seconds = minutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(
            totalSeconds = seconds,
            remainingSeconds = seconds,
            pausedRemainingSeconds = seconds,
            targetEndTimeMillis = null,
            isRunning = false
        )
        pomodoroTimerJob?.cancel()
    }

    fun addPomodoroTime(minutes: Int) {
        val addSec = minutes * 60
        val current = _pomodoroState.value
        val newTotal = current.totalSeconds + addSec
        if (current.isRunning) {
            val newRem = current.remainingSeconds + addSec
            val newTarget = (current.targetEndTimeMillis ?: System.currentTimeMillis()) + (addSec * 1000L)
            _pomodoroState.value = current.copy(
                totalSeconds = newTotal,
                remainingSeconds = newRem,
                targetEndTimeMillis = newTarget
            )
        } else {
            val newRem = current.remainingSeconds + addSec
            _pomodoroState.value = current.copy(
                totalSeconds = newTotal,
                remainingSeconds = newRem,
                pausedRemainingSeconds = newRem
            )
        }
    }

    fun importDataFromJson(jsonString: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(jsonString)
                var count = 0
                if (root.has("notes")) {
                    val notesArr = root.getJSONArray("notes")
                    for (i in 0 until notesArr.length()) {
                        val n = notesArr.getJSONObject(i)
                        repository.insertNote(
                            NoteEntity(
                                title = n.optString("title", "Imported Note"),
                                contentMarkdown = n.optString("content", ""),
                                subjectName = n.optString("subject", "General")
                            )
                        )
                        count++
                    }
                }
                if (root.has("flashcards")) {
                    val fcArr = root.getJSONArray("flashcards")
                    for (i in 0 until fcArr.length()) {
                        val fc = fcArr.getJSONObject(i)
                        repository.insertFlashcard(
                            FlashcardEntity(
                                front = fc.optString("front", "Imported Q"),
                                back = fc.optString("back", "Imported A")
                            )
                        )
                        count++
                    }
                }
                onDone(true, "Successfully imported $count study item(s) from JSON!")
            } catch (e: Exception) {
                onDone(false, "Import failed: ${e.localizedMessage}")
            }
        }
    }
}
