# StudyForge System Architecture

## Architecture Overview

StudyForge is designed according to modern Android MVVM and Clean Architecture principles:

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (Screens, Navigation Bars, Copilot Modal, Theme)     │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / UI Events
┌───────────────────────────┴────────────────────────────┐
│                  StudyForgeViewModel                   │
│    (Active Test Loop, Pomodoro Timer, Route Manager)   │
└─────────────▲────────────────────────────▲─────────────┘
              │                            │
              ▼                            ▼
┌───────────────────────────┐┌───────────────────────────┐
│   StudyForgeRepository    ││    AiProviderManager      │
│  (Cross-Module Events)    ││ (Gemini, OpenAI, Fallback)│
└─────────────▲─────────────┘└───────────────────────────┘
              │
              ▼
┌───────────────────────────┐
│     Room AppDatabase      │
│  (18 Consolidated Tables) │
└───────────────────────────┘
```

### Module Responsibilities:
1. **`com.example.data`**:
   - `Entities.kt`: Single-source-of-truth schema definitions for subjects, chapters, topics, questions, tests, attempts, mistakes, cards, tasks, notes, sessions, logs, and profile.
   - `StudyForgeDao.kt`: Comprehensive DAO with reactive Kotlin Flows and thread-safe Room queries.
   - `StudyForgeDatabase.kt`: Abstract database with seed callback for initial bootstrap.
   - `StudyForgeRepository.kt`: Encapsulates cross-module event triggers (e.g. `recordTestCompletion`, `recordQuestionAttempt`, `recordStudySession`, `reviewFlashcard`).

2. **`com.example.ai`**:
   - `AiModels.kt`: Provider enums, model metadata catalog, and 10 bot persona system instructions.
   - `AiProviderManager.kt`: Multi-provider HTTP client supporting direct Google Gemini and OpenAI REST endpoints with context injection and academic offline engine fallback.

3. **`com.example.ui`**:
   - Central `StudyForgeViewModel`: Exposes reactive `StateFlow` streams to Compose.
   - Screen composables in `com.example.ui.screens`: `DashboardScreen`, `SyllabusScreen`, `QuestionBankScreen`, `TestEngineScreen`, `ActiveTestScreen`, `MistakeBookScreen`, `FlashcardsScreen`, `PlannerScreen`, `PomodoroScreen`, `NotesScreen`, `DocumentReaderScreen`, `AiBotsScreen`, `AnalyticsScreen`, `SettingsScreen`.
   - Reusable components in `com.example.ui.components`: `AppNavigationBars`, `AiCopilotModal`.
