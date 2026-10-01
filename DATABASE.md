# StudyForge Database Schema (Room)

## Table Entities

1. `subjects`: ID, name, code, colorHex, iconName, totalChapters, completedChapters.
2. `chapters`: ID, subjectId, title, orderIndex, completionPercent, masteryLevel, confidenceScore, timeSpentMinutes.
3. `topics`: ID, chapterId, title, subtopicsJson, isCompleted, masteryScore, accuracyRate, revisionStatus, difficultyLevel, isWeak, isBookmarked.
4. `questions`: ID, subjectId, chapterId, topicId, questionText, type, difficulty, source, year, isPyq, marks, negativeMarks, explanation, hint, optionsJson, correctAnswer, isBookmarked.
5. `question_attempts`: ID, questionId, selectedAnswer, isCorrect, timeSpentSec, timestamp, mistakeCategory, testAttemptId.
6. `tests`: ID, title, testType, durationMinutes, totalMarks, totalQuestions, subjectFilter, chapterFilter, createdAt.
7. `test_attempts`: ID, testId, testTitle, score, maxScore, accuracy, completedAt, durationSec, attemptedCount, skippedCount, incorrectCount, analysisNotes.
8. `mistakes`: ID, questionId, questionText, subjectName, topicName, userAnswer, correctAnswer, explanation, category, repeatCount, reviewStatus, createdAt, lastReviewedAt.
9. `flashcards`: ID, deckId, subjectId, front, back, clozeText, cardType, intervalDays, easeFactor, repetitions, lapses, dueDate, lastReviewedAt, isBookmarked.
10. `decks`: ID, name, subjectId, description, cardCount.
11. `planner_tasks`: ID, title, taskType, subjectName, chapterName, scheduledDate, startTime, durationMinutes, isCompleted, isMissed, priority.
12. `study_sessions`: ID, subjectId, chapterId, subjectName, chapterName, durationMinutes, sessionType, notes, timestamp, xpEarned.
13. `notes`: ID, title, contentMarkdown, subjectName, chapterName, folder, tags, isPinned, isFavorite, updatedAt.
14. `study_documents`: ID, title, fileType, pageCount, summary, contentExtract, tags, createdAt.
15. `bookmarks`: ID, itemType, itemId, title, subtitle, targetRoute, timestamp.
16. `notification_items`: ID, title, message, type, priority, isRead, timestamp, actionRoute.
17. `user_profile`: ID (1), currentStreak, longestStreak, totalXp, level, dailyTargetMinutes, examTargetName, examTargetDaysRemaining, lastActiveDate.
18. `ai_conversations` & `ai_messages`: Multi-chat history for 10 bots.
19. `ai_logs`: Audit log storing provider, model, latency, tokens, and status.
