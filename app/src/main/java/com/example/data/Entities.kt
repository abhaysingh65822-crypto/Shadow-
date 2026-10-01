package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val iconName: String,
    val totalChapters: Int = 0,
    val completedChapters: Int = 0
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val orderIndex: Int,
    val completionPercent: Int = 0,
    val masteryLevel: Int = 0, // 0 - 100
    val confidenceScore: Int = 50, // 0 - 100
    val timeSpentMinutes: Int = 0
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val title: String,
    val subtopicsJson: String = "[]", // JSON array of string names
    val isCompleted: Boolean = false,
    val masteryScore: Int = 0, // 0 - 100
    val accuracyRate: Int = 0, // 0 - 100
    val revisionStatus: String = "PENDING", // PENDING, DUE, MASTERED
    val difficultyLevel: String = "MEDIUM", // EASY, MEDIUM, HARD
    val isWeak: Boolean = false,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val chapterId: Long,
    val topicId: Long,
    val questionText: String,
    val type: String = "MCQ", // MCQ, NUMERICAL, TRUE_FALSE
    val difficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val source: String = "StudyForge Bank",
    val year: String = "2024",
    val isPyq: Boolean = false,
    val marks: Int = 4,
    val negativeMarks: Float = 1.0f,
    val explanation: String = "",
    val hint: String = "",
    val optionsJson: String = "[]", // JSON array of strings
    val correctAnswer: String = "",
    val isBookmarked: Boolean = false
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val timeSpentSec: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val mistakeCategory: String? = null,
    val testAttemptId: Long? = null
)

@Entity(tableName = "tests")
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val testType: String = "CHAPTER_TEST", // CHAPTER_TEST, SUBJECT_TEST, PYQ_TEST, FULL_SYLLABUS
    val durationMinutes: Int = 30,
    val totalMarks: Int = 40,
    val totalQuestions: Int = 10,
    val subjectFilter: String = "",
    val chapterFilter: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val testTitle: String,
    val score: Int,
    val maxScore: Int,
    val accuracy: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val durationSec: Int,
    val attemptedCount: Int,
    val skippedCount: Int,
    val incorrectCount: Int,
    val analysisNotes: String = ""
)

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val questionText: String,
    val subjectName: String,
    val topicName: String,
    val userAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val category: String = "CONCEPTUAL", // CONCEPTUAL, CALCULATION, CARELESS, MEMORY, INTERPRETATION, TIME_PRESSURE
    val repeatCount: Int = 1,
    val reviewStatus: String = "NEEDS_PRACTICE", // NEEDS_PRACTICE, REVIEWED, RESOLVED
    val createdAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long = 1,
    val subjectId: Long = 1,
    val front: String,
    val back: String,
    val clozeText: String? = null,
    val cardType: String = "STANDARD", // STANDARD, CLOZE, FORMULA
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val dueDate: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subjectId: Long,
    val description: String = "",
    val cardCount: Int = 0
)

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val taskType: String = "STUDY", // STUDY, REVISION, PRACTICE, TEST
    val subjectName: String,
    val chapterName: String = "",
    val scheduledDate: Long, // timestamp midnight
    val startTime: String = "10:00 AM",
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val priority: String = "HIGH" // HIGH, MEDIUM, LOW
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val chapterId: Long? = null,
    val subjectName: String,
    val chapterName: String = "",
    val durationMinutes: Int,
    val sessionType: String = "POMODORO", // POMODORO, DEEP_WORK, REVISION
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val xpEarned: Int = 50
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val contentMarkdown: String,
    val subjectName: String = "Physics",
    val chapterName: String = "",
    val folder: String = "General",
    val tags: String = "[]",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_documents")
data class StudyDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileType: String = "PDF",
    val pageCount: Int = 1,
    val summary: String = "",
    val contentExtract: String = "",
    val tags: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: String, // QUESTION, NOTE, FLASHCARD, TOPIC, DOCUMENT
    val itemId: Long,
    val title: String,
    val subtitle: String = "",
    val targetRoute: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_items")
data class NotificationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "INFO", // REVISION_DUE, WEAK_TOPIC, STREAK, TEST_RESULT, PLANNER_MISSED, AI_RECOMMENDATION
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val actionRoute: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1,
    val name: String = "Student",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalXp: Int = 0,
    val level: Int = 1,
    val dailyTargetMinutes: Int = 60,
    val examTargetName: String = "",
    val examTargetDaysRemaining: Int = 0,
    val examDateMillis: Long? = null,
    val gradeClass: String = "",
    val studyGoals: String = "",
    val preferredSubjects: String = "",
    val difficultyPreference: String = "MEDIUM",
    val lastActiveDate: Long = 0L
)

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botType: String, // AI_TUTOR, AI_PLANNER, AI_EXAMINER, etc.
    val title: String,
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val botType: String,
    val sender: String, // USER, BOT
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val latencyMs: Long = 420,
    val tokensUsed: Int = 120
)

@Entity(tableName = "ai_logs")
data class AiLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val provider: String,
    val model: String,
    val bot: String,
    val promptPreview: String,
    val status: String, // SUCCESS, FAILED, FALLBACK
    val latencyMs: Long,
    val tokens: Int,
    val timestamp: Long = System.currentTimeMillis()
)
