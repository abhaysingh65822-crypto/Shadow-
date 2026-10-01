package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        QuestionEntity::class,
        QuestionAttemptEntity::class,
        TestEntity::class,
        TestAttemptEntity::class,
        MistakeEntity::class,
        FlashcardEntity::class,
        DeckEntity::class,
        PlannerTaskEntity::class,
        StudySessionEntity::class,
        NoteEntity::class,
        StudyDocumentEntity::class,
        BookmarkEntity::class,
        NotificationItemEntity::class,
        UserProfileEntity::class,
        AiConversationEntity::class,
        AiMessageEntity::class,
        AiLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudyForgeDatabase : RoomDatabase() {

    abstract fun studyForgeDao(): StudyForgeDao

    companion object {
        @Volatile
        private var INSTANCE: StudyForgeDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StudyForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyForgeDatabase::class.java,
                    "studyforge_database"
                )
                    .addCallback(StudyForgeDatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class StudyForgeDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studyForgeDao())
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studyForgeDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: StudyForgeDao) {
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
                // Note: Mistakes, test attempts, and study sessions start completely empty for a new user
            }
        }
    }
}
