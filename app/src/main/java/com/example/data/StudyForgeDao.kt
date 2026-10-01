package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyForgeDao {

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: Long)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersBySubject(subjectId: Long)

    @Query("DELETE FROM topics WHERE chapterId IN (SELECT id FROM chapters WHERE subjectId = :subjectId)")
    suspend fun deleteTopicsBySubject(subjectId: Long)

    @Query("DELETE FROM questions WHERE subjectId = :subjectId")
    suspend fun deleteQuestionsBySubject(subjectId: Long)

    // Chapters
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getChaptersBySubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapter(id: Long)

    @Query("DELETE FROM topics WHERE chapterId = :chapterId")
    suspend fun deleteTopicsByChapter(chapterId: Long)

    @Query("UPDATE chapters SET timeSpentMinutes = timeSpentMinutes + :minutes WHERE id = :chapterId")
    suspend fun addChapterStudyTime(chapterId: Long, minutes: Int)

    // Topics
    @Query("SELECT * FROM topics WHERE chapterId = :chapterId")
    fun getTopicsByChapter(chapterId: Long): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE isWeak = 1")
    fun getWeakTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopic(id: Long)

    // Questions
    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId")
    fun getQuestionsBySubject(subjectId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    fun getQuestionsByChapter(chapterId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isPyq = 1")
    fun getPyqQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestion(id: Long)

    // Question Attempts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAttempt(attempt: QuestionAttemptEntity): Long

    @Query("SELECT * FROM question_attempts ORDER BY timestamp DESC")
    fun getAllQuestionAttempts(): Flow<List<QuestionAttemptEntity>>

    // Tests
    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestEntity>)

    @Query("DELETE FROM tests WHERE id = :id")
    suspend fun deleteTest(id: Long)

    // Test Attempts
    @Query("SELECT * FROM test_attempts ORDER BY completedAt DESC")
    fun getAllTestAttempts(): Flow<List<TestAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttemptEntity): Long

    // Mistakes
    @Query("SELECT * FROM mistakes ORDER BY createdAt DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE reviewStatus != 'RESOLVED'")
    fun getUnresolvedMistakes(): Flow<List<MistakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistakes(mistakes: List<MistakeEntity>)

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Query("DELETE FROM mistakes WHERE id = :id")
    suspend fun deleteMistake(id: Long)

    // Flashcards & Decks
    @Query("SELECT * FROM decks")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<DeckEntity>)

    @Query("SELECT * FROM flashcards ORDER BY dueDate ASC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY dueDate ASC")
    fun getFlashcardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE dueDate <= :currentTime ORDER BY dueDate ASC")
    fun getDueFlashcards(currentTime: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: Long)

    // Planner Tasks
    @Query("SELECT * FROM planner_tasks ORDER BY scheduledDate ASC, id ASC")
    fun getAllPlannerTasks(): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannerTask(task: PlannerTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannerTasks(tasks: List<PlannerTaskEntity>)

    @Update
    suspend fun updatePlannerTask(task: PlannerTaskEntity)

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deletePlannerTask(id: Long)

    // Study Sessions
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity): Long

    // Notes
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // Study Documents
    @Query("SELECT * FROM study_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<StudyDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: StudyDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<StudyDocumentEntity>)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE itemType = :type AND itemId = :id")
    suspend fun deleteBookmarkByTypeAndId(type: String, id: Long)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    // Notifications
    @Query("SELECT * FROM notification_items ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItemEntity): Long

    @Query("UPDATE notification_items SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notification_items SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // AI Conversations & Messages
    @Query("SELECT * FROM ai_conversations WHERE botType = :botType ORDER BY updatedAt DESC")
    fun getConversationsByBot(botType: String): Flow<List<AiConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: AiConversationEntity): Long

    @Query("SELECT * FROM ai_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesByConversation(convId: Long): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMessage(msg: AiMessageEntity): Long

    // AI Logs
    @Query("SELECT * FROM ai_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAiLogs(): Flow<List<AiLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiLog(log: AiLogEntity): Long

    @Query("DELETE FROM ai_logs")
    suspend fun clearAiLogs()

    // Backup & Reset Helpers
    @Query("DELETE FROM subjects") suspend fun clearSubjects()
    @Query("DELETE FROM chapters") suspend fun clearChapters()
    @Query("DELETE FROM topics") suspend fun clearTopics()
    @Query("DELETE FROM questions") suspend fun clearQuestions()
    @Query("DELETE FROM question_attempts") suspend fun clearAttempts()
    @Query("DELETE FROM tests") suspend fun clearTests()
    @Query("DELETE FROM test_attempts") suspend fun clearTestAttempts()
    @Query("DELETE FROM mistakes") suspend fun clearMistakes()
    @Query("DELETE FROM flashcards") suspend fun clearFlashcards()
    @Query("DELETE FROM decks") suspend fun clearDecks()
    @Query("DELETE FROM planner_tasks") suspend fun clearPlannerTasks()
    @Query("DELETE FROM study_sessions") suspend fun clearStudySessions()
    @Query("DELETE FROM notes") suspend fun clearNotes()
    @Query("DELETE FROM study_documents") suspend fun clearDocuments()
    @Query("DELETE FROM bookmarks") suspend fun clearBookmarks()
    @Query("DELETE FROM notification_items") suspend fun clearNotifications()
}
