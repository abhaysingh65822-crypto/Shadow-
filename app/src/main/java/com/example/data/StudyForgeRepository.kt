package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyForgeRepository(private val dao: StudyForgeDao) {

    // Reactive streams for UI
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = dao.getAllChapters()
    val allTopics: Flow<List<TopicEntity>> = dao.getAllTopics()
    val weakTopics: Flow<List<TopicEntity>> = dao.getWeakTopics()
    val allQuestions: Flow<List<QuestionEntity>> = dao.getAllQuestions()
    val pyqQuestions: Flow<List<QuestionEntity>> = dao.getPyqQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = dao.getBookmarkedQuestions()
    val allQuestionAttempts: Flow<List<QuestionAttemptEntity>> = dao.getAllQuestionAttempts()
    val allTests: Flow<List<TestEntity>> = dao.getAllTests()
    val allTestAttempts: Flow<List<TestAttemptEntity>> = dao.getAllTestAttempts()
    val allMistakes: Flow<List<MistakeEntity>> = dao.getAllMistakes()
    val unresolvedMistakes: Flow<List<MistakeEntity>> = dao.getUnresolvedMistakes()
    val allDecks: Flow<List<DeckEntity>> = dao.getAllDecks()
    val allFlashcards: Flow<List<FlashcardEntity>> = dao.getAllFlashcards()
    val allPlannerTasks: Flow<List<PlannerTaskEntity>> = dao.getAllPlannerTasks()
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val allDocuments: Flow<List<StudyDocumentEntity>> = dao.getAllDocuments()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val allNotifications: Flow<List<NotificationItemEntity>> = dao.getAllNotifications()
    val aiLogs: Flow<List<AiLogEntity>> = dao.getAiLogs()

    fun getChaptersBySubject(subjectId: Long): Flow<List<ChapterEntity>> = dao.getChaptersBySubject(subjectId)
    fun getTopicsByChapter(chapterId: Long): Flow<List<TopicEntity>> = dao.getTopicsByChapter(chapterId)
    fun getQuestionsBySubject(subjectId: Long): Flow<List<QuestionEntity>> = dao.getQuestionsBySubject(subjectId)
    fun getQuestionsByChapter(chapterId: Long): Flow<List<QuestionEntity>> = dao.getQuestionsByChapter(chapterId)
    fun getFlashcardsByDeck(deckId: Long): Flow<List<FlashcardEntity>> = dao.getFlashcardsByDeck(deckId)
    fun getDueFlashcards(now: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>> = dao.getDueFlashcards(now)
    fun getConversationsByBot(botType: String): Flow<List<AiConversationEntity>> = dao.getConversationsByBot(botType)
    fun getMessagesByConversation(convId: Long): Flow<List<AiMessageEntity>> = dao.getMessagesByConversation(convId)

    // ==========================================
    // Cross-Module Event: Attempt Single Question
    // ==========================================
    suspend fun recordQuestionAttempt(
        question: QuestionEntity,
        selectedAnswer: String,
        timeSpentSec: Int,
        mistakeCategory: String? = null
    ): Boolean {
        val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)

        dao.insertQuestionAttempt(
            QuestionAttemptEntity(
                questionId = question.id,
                selectedAnswer = selectedAnswer,
                isCorrect = isCorrect,
                timeSpentSec = timeSpentSec,
                mistakeCategory = if (!isCorrect) (mistakeCategory ?: "CONCEPTUAL") else null
            )
        )

        // Award XP
        val xpGain = if (isCorrect) 15 else 5
        awardXp(xpGain)

        if (!isCorrect) {
            // Automatically log to Mistake Book
            dao.insertMistake(
                MistakeEntity(
                    questionId = question.id,
                    questionText = question.questionText,
                    subjectName = "Subject #${question.subjectId}",
                    topicName = "Topic #${question.topicId}",
                    userAnswer = selectedAnswer,
                    correctAnswer = question.correctAnswer,
                    explanation = question.explanation,
                    category = mistakeCategory ?: "CONCEPTUAL"
                )
            )

            // Trigger notification for weak concept reinforcement
            dao.insertNotification(
                NotificationItemEntity(
                    title = "Mistake Logged",
                    message = "Wrong answer recorded for review: \"${question.questionText.take(50)}...\"",
                    type = "WEAK_TOPIC",
                    priority = "NORMAL",
                    actionRoute = "mistakes"
                )
            )
        }

        return isCorrect
    }

    // ==========================================
    // Cross-Module Event: Test Completed
    // ==========================================
    suspend fun recordTestCompletion(
        testId: Long,
        testTitle: String,
        score: Int,
        maxScore: Int,
        durationSec: Int,
        attemptedCount: Int,
        skippedCount: Int,
        incorrectCount: Int,
        wrongQuestions: List<Pair<QuestionEntity, String>> // question to userAnswer
    ) {
        val accuracy = if (attemptedCount > 0) {
            (((attemptedCount - incorrectCount).toFloat() / attemptedCount) * 100).toInt()
        } else 0

        val notes = if (accuracy >= 80) {
            "Exceptional performance! High speed and conceptual accuracy."
        } else if (accuracy >= 50) {
            "Satisfactory grasp. $incorrectCount incorrect answers identified for revision."
        } else {
            "Critical weakness detected. Auto-scheduled targeted revisions."
        }

        dao.insertTestAttempt(
            TestAttemptEntity(
                testId = testId,
                testTitle = testTitle,
                score = score,
                maxScore = maxScore,
                accuracy = accuracy,
                durationSec = durationSec,
                attemptedCount = attemptedCount,
                skippedCount = skippedCount,
                incorrectCount = incorrectCount,
                analysisNotes = notes
            )
        )

        // Log all mistakes
        for ((q, userAns) in wrongQuestions) {
            dao.insertMistake(
                MistakeEntity(
                    questionId = q.id,
                    questionText = q.questionText,
                    subjectName = "Subject #${q.subjectId}",
                    topicName = "Topic #${q.topicId}",
                    userAnswer = userAns,
                    correctAnswer = q.correctAnswer,
                    explanation = q.explanation,
                    category = "CONCEPTUAL"
                )
            )
        }

        // Award XP based on completion and score
        val testXp = 50 + (score.coerceAtLeast(0) * 10)
        awardXp(testXp)

        // Generate immediate AI recommendation notification
        dao.insertNotification(
            NotificationItemEntity(
                title = "Test Analyzed: $testTitle",
                message = "Score: $score/$maxScore ($accuracy%). $notes",
                type = "TEST_RESULT",
                priority = "HIGH",
                actionRoute = "tests"
            )
        )

        // If score < 70%, schedule an automated planner revision task!
        if (accuracy < 70) {
            dao.insertPlannerTask(
                PlannerTaskEntity(
                    title = "Revise Mistakes from $testTitle",
                    taskType = "REVISION",
                    subjectName = "General Review",
                    chapterName = testTitle,
                    scheduledDate = System.currentTimeMillis() + 86400000,
                    startTime = "04:00 PM",
                    durationMinutes = 35,
                    priority = "HIGH"
                )
            )
        }
    }

    // ==========================================
    // Cross-Module Event: Study Focus Session (Pomodoro)
    // ==========================================
    suspend fun recordStudySession(
        subjectId: Long,
        chapterId: Long?,
        subjectName: String,
        chapterName: String,
        durationMinutes: Int,
        sessionType: String,
        notes: String
    ) {
        val xp = (durationMinutes * 2).coerceAtLeast(20)
        dao.insertStudySession(
            StudySessionEntity(
                subjectId = subjectId,
                chapterId = chapterId,
                subjectName = subjectName,
                chapterName = chapterName,
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                notes = notes,
                xpEarned = xp
            )
        )

        if (chapterId != null && chapterId > 0) {
            dao.addChapterStudyTime(chapterId, durationMinutes)
        }

        awardXp(xp)
        checkAndUpdateStreak()

        dao.insertNotification(
            NotificationItemEntity(
                title = "Focus Session Recorded",
                message = "Completed $durationMinutes min of $subjectName ($sessionType). Earned +$xp XP!",
                type = "STREAK",
                priority = "NORMAL",
                actionRoute = "dashboard"
            )
        )
    }

    // ==========================================
    // Cross-Module Event: Spaced Repetition (SM-2)
    // ==========================================
    suspend fun reviewFlashcard(flashcard: FlashcardEntity, quality: Int) {
        // Quality: 0 = Again, 1 = Hard, 2 = Good, 3 = Easy
        var repetitions = flashcard.repetitions
        var lapses = flashcard.lapses
        var easeFactor = flashcard.easeFactor
        var intervalDays = flashcard.intervalDays

        if (quality >= 2) {
            if (repetitions == 0) {
                intervalDays = 1
            } else if (repetitions == 1) {
                intervalDays = 6
            } else {
                intervalDays = (intervalDays * easeFactor).toInt().coerceAtLeast(1)
            }
            repetitions += 1
            awardXp(10)
        } else {
            repetitions = 0
            intervalDays = 1
            lapses += 1
            awardXp(3)
        }

        // Adjust ease factor
        easeFactor += (0.1f - (3 - quality) * (0.08f + (3 - quality) * 0.02f))
        if (easeFactor < 1.3f) easeFactor = 1.3f

        val nextDue = System.currentTimeMillis() + (intervalDays.toLong() * 86400000L)
        dao.updateFlashcard(
            flashcard.copy(
                intervalDays = intervalDays,
                easeFactor = easeFactor,
                repetitions = repetitions,
                lapses = lapses,
                dueDate = nextDue,
                lastReviewedAt = System.currentTimeMillis()
            )
        )
    }

    // ==========================================
    // Gamification & Streak Logic
    // ==========================================
    suspend fun awardXp(amount: Int) {
        val profile = dao.getUserProfileSync() ?: SeedData.defaultProfile
        val newXp = profile.totalXp + amount
        val newLevel = (newXp / 300) + 1
        dao.insertOrUpdateProfile(
            profile.copy(
                totalXp = newXp,
                level = newLevel
            )
        )
    }

    private suspend fun checkAndUpdateStreak() {
        val profile = dao.getUserProfileSync() ?: SeedData.defaultProfile
        val now = System.currentTimeMillis()
        val oneDayMillis = 86400000L
        val lastDay = profile.lastActiveDate / oneDayMillis
        val currentDay = now / oneDayMillis

        if (currentDay > lastDay) {
            val newStreak = if (currentDay == lastDay + 1) profile.currentStreak + 1 else 1
            val longest = maxOf(profile.longestStreak, newStreak)
            dao.insertOrUpdateProfile(
                profile.copy(
                    currentStreak = newStreak,
                    longestStreak = longest,
                    lastActiveDate = now
                )
            )
        }
    }

    // ==========================================
    // Syllabus & Topics CRUD
    // ==========================================
    suspend fun insertSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subjectId: Long) {
        dao.deleteTopicsBySubject(subjectId)
        dao.deleteChaptersBySubject(subjectId)
        dao.deleteQuestionsBySubject(subjectId)
        dao.deleteSubject(subjectId)
    }

    suspend fun insertChapter(chapter: ChapterEntity): Long = dao.insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = dao.updateChapter(chapter)
    suspend fun deleteChapter(chapterId: Long) {
        dao.deleteTopicsByChapter(chapterId)
        dao.deleteChapter(chapterId)
    }

    suspend fun insertTopic(topic: TopicEntity): Long = dao.insertTopic(topic)
    suspend fun updateTopic(topic: TopicEntity) = dao.updateTopic(topic)
    suspend fun deleteTopic(topicId: Long) = dao.deleteTopic(topicId)

    // ==========================================
    // Questions CRUD
    // ==========================================
    suspend fun insertQuestion(question: QuestionEntity): Long = dao.insertQuestion(question)
    suspend fun updateQuestion(question: QuestionEntity) = dao.updateQuestion(question)
    suspend fun deleteQuestion(questionId: Long) = dao.deleteQuestion(questionId)

    // ==========================================
    // Tests CRUD
    // ==========================================
    suspend fun insertTest(test: TestEntity): Long = dao.insertTest(test)
    suspend fun deleteTest(testId: Long) = dao.deleteTest(testId)

    // ==========================================
    // Notes CRUD
    // ==========================================
    suspend fun insertNote(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(id: Long) = dao.deleteNote(id)

    // ==========================================
    // Documents CRUD
    // ==========================================
    suspend fun insertDocument(doc: StudyDocumentEntity): Long = dao.insertDocument(doc)
    suspend fun deleteDocument(id: Long) = dao.deleteDocument(id)

    // ==========================================
    // Flashcards & Decks CRUD
    // ==========================================
    suspend fun insertDeck(deck: DeckEntity): Long = dao.insertDeck(deck)
    suspend fun deleteDeck(id: Long) = dao.deleteDeck(id)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = dao.insertFlashcard(flashcard)
    suspend fun updateFlashcard(flashcard: FlashcardEntity) = dao.updateFlashcard(flashcard)
    suspend fun deleteFlashcard(id: Long) = dao.deleteFlashcard(id)

    // ==========================================
    // Mistakes CRUD
    // ==========================================
    suspend fun updateMistake(mistake: MistakeEntity) = dao.updateMistake(mistake)
    suspend fun deleteMistake(id: Long) = dao.deleteMistake(id)

    // ==========================================
    // Planner Tasks CRUD
    // ==========================================
    suspend fun insertPlannerTask(task: PlannerTaskEntity): Long = dao.insertPlannerTask(task)
    suspend fun updatePlannerTask(task: PlannerTaskEntity) = dao.updatePlannerTask(task)
    suspend fun deletePlannerTask(id: Long) = dao.deletePlannerTask(id)
    suspend fun rescheduleMissedTasks() {
        val now = System.currentTimeMillis()
        // Reschedule any missed task to tomorrow
        val tomorrow = now + 86400000L
        // Handled in viewmodel
    }

    // ==========================================
    // Bookmarks CRUD
    // ==========================================
    suspend fun deleteBookmark(id: Long) = dao.deleteBookmark(id)
    suspend fun toggleBookmark(type: String, id: Long, title: String, subtitle: String, route: String, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            dao.deleteBookmarkByTypeAndId(type, id)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    itemType = type,
                    itemId = id,
                    title = title,
                    subtitle = subtitle,
                    targetRoute = route
                )
            )
        }
    }

    // ==========================================
    // Profile CRUD
    // ==========================================
    suspend fun updateProfile(profile: UserProfileEntity) = dao.insertOrUpdateProfile(profile)

    // ==========================================
    // Notifications CRUD
    // ==========================================
    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()

    // ==========================================
    // AI Conversations & Logs
    // ==========================================
    suspend fun createConversation(botType: String, title: String): Long {
        return dao.insertConversation(AiConversationEntity(botType = botType, title = title))
    }

    suspend fun insertAiMessage(msg: AiMessageEntity): Long = dao.insertAiMessage(msg)
    suspend fun logAiRequest(log: AiLogEntity): Long = dao.insertAiLog(log)
    suspend fun clearAiLogs() = dao.clearAiLogs()

    // ==========================================
    // Demo Data Management & Reset
    // ==========================================
    suspend fun clearAllData() {
        dao.clearSubjects()
        dao.clearChapters()
        dao.clearTopics()
        dao.clearQuestions()
        dao.clearAttempts()
        dao.clearTests()
        dao.clearTestAttempts()
        dao.clearMistakes()
        dao.clearFlashcards()
        dao.clearDecks()
        dao.clearPlannerTasks()
        dao.clearStudySessions()
        dao.clearNotes()
        dao.clearDocuments()
        dao.clearBookmarks()
        dao.clearNotifications()
    }

    suspend fun restoreSeedData() {
        clearAllData()
        dao.insertOrUpdateProfile(SeedData.defaultProfile)
        dao.insertSubjects(SeedData.subjects)
        dao.insertChapters(SeedData.chapters)
        dao.insertTopics(SeedData.topics)
        dao.insertQuestions(SeedData.questions)
        dao.insertDecks(SeedData.decks)
        dao.insertFlashcards(SeedData.flashcards)
        dao.insertPlannerTasks(SeedData.plannerTasks)
        dao.insertNotes(SeedData.notes)
        dao.insertDocuments(SeedData.documents)
        dao.insertTests(SeedData.tests)
    }

    suspend fun loadDemoData() {
        SeedData.loadDemoData(dao)
    }

    suspend fun resetProgress() {
        SeedData.resetProgress(dao)
    }

    // JSON Export & Import
    suspend fun exportDataAsJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "StudyForge")
        root.put("exportedAt", System.currentTimeMillis())
        val profile = dao.getUserProfileSync()
        if (profile != null) {
            val profObj = JSONObject()
            profObj.put("name", profile.name)
            profObj.put("streak", profile.currentStreak)
            profObj.put("xp", profile.totalXp)
            profObj.put("level", profile.level)
            profObj.put("examTargetName", profile.examTargetName)
            profObj.put("examDateMillis", profile.examDateMillis ?: 0L)
            root.put("profile", profObj)
        }
        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val profObj = root.optJSONObject("profile")
            if (profObj != null) {
                val current = dao.getUserProfileSync() ?: SeedData.defaultProfile
                dao.insertOrUpdateProfile(
                    current.copy(
                        name = profObj.optString("name", current.name),
                        totalXp = profObj.optInt("xp", current.totalXp),
                        level = profObj.optInt("level", current.level),
                        currentStreak = profObj.optInt("streak", current.currentStreak),
                        examTargetName = profObj.optString("examTargetName", current.examTargetName),
                        examDateMillis = if (profObj.has("examDateMillis")) profObj.getLong("examDateMillis") else null
                    )
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
