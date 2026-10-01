# STUDYFORGE — AI Powered Personal Study Operating System

StudyForge is a production-grade, local-first, deeply interconnected Android Study Operating System built with Jetpack Compose, Room Database, and a unified Multi-Provider AI Architecture supporting Google Gemini and OpenAI.

---

## 🌟 Core Philosophy: The Unified Learning Loop
In StudyForge, every module communicates with all other modules through a centralized reactive data layer and event coordinator:

```
Test Attempt / Question Attempt
       │
       ▼ (if wrong answer)
Mistake Book (Categorized: Conceptual, Memory, Calculation, Careless)
       │
       ▼
Weak Topics Flagged (Accuracy dropped < 60%)
       │
       ▼
AI Revision Coach & Flashcard Engine (SM-2 Interval Scheduling)
       │
       ▼
Smart Planner (Auto-reschedules review sessions)
       │
       ▼
Command Center Dashboard & Daily AI Briefing
```

---

## 🚀 Key Modules & Capabilities

1. **Command Center Dashboard**:
   - Live streak counter, XP progress, level status, and exam target countdown widget.
   - Daily AI study briefing and adaptive recommendations generated from real telemetry.
   - Subject mastery matrix and weak concept warning indicators.

2. **Smart Hierarchical Syllabus**:
   - Subject → Chapter → Topic → Subtopics hierarchy.
   - Live mastery score, completion percentage, confidence level, and study time tracking.
   - Direct chapter and topic actions: Practice questions, ask AI Tutor, review flashcards.

3. **Adaptive Question Bank**:
   - Subject, difficulty (Easy, Medium, Hard), and PYQ (Previous Year Questions) filtering.
   - Instant answer verification with step-by-step mathematical/conceptual explanations and hints.
   - Automatic logging of incorrect attempts to the Mistake Book.

4. **Exam Simulator & Test Engine**:
   - Timed mock examinations with real countdown timer.
   - Numbered question palette with live status: Answered, Unanswered, Marked for Review.
   - Auto-submit on timer expiry and detailed score card with accuracy, speed, and question-by-question solution breakdown.

5. **Spaced Repetition & Flashcards (SM-2 Algorithm)**:
   - SuperMemo-2 spaced repetition algorithm with dynamic ease factors and intervals.
   - 3D card flip animation with interactive rating: Again (<1d), Hard (1d), Good (3d), Easy (6d).
   - AI Flashcard Generator: Automatically generates atomic active recall cards from any concept.

6. **Mistake Book & Error Classifier**:
   - Classifies errors into Conceptual, Calculation, Careless, Memory, and Time Pressure categories.
   - One-tap "AI Diagnosis" to explain root misconceptions.
   - Resolution workflow to clear weaknesses.

7. **Smart Study Timetable & Planner**:
   - Daily and weekly study blocks categorized by task type (Study, Practice, Revision, Test).
   - Intelligent auto-reschedule of missed tasks to prevent student backlog.

8. **Pomodoro & Focus Timer**:
   - Customizable focus sessions (25m), short breaks (5m), and long breaks (15m).
   - Subject and chapter tagging.
   - Completed focus blocks directly increment chapter study hours and award XP.

9. **Markdown Study Notebook**:
   - Searchable notes with folder categorization, tags, and favorites.
   - One-tap AI summarization, simplification, and conversion into flashcards.

10. **Document / PDF Reader**:
    - Study document and lecture excerpt viewer.
    - Context-aware document AI: Ask questions directly against the document text.

11. **Ten Specialized AI Bots**:
    - **AI Tutor**: Socratic concept teacher.
    - **AI Planner**: Exam readiness and study schedule optimizer.
    - **AI Examiner**: Custom test and question creator with grading rubrics.
    - **AI Revision Coach**: Spaced repetition and weak concept drillmaster.
    - **AI Notes Assistant**: Concept summarizer and formula condenser.
    - **AI Flashcard Generator**: Cloze deletion and Anki card creator.
    - **AI Coding Tutor**: Algorithms, complexity analysis, and pointer logic.
    - **AI Mathematics Solver**: Step-by-step calculus proofs and derivations.
    - **AI Physics Tutor**: Free-body diagrams, work-energy, and mechanics.
    - **AI Chemistry Tutor**: Equilibrium shifts, reaction mechanisms, and kinetics.

12. **Multi-Provider AI Architecture**:
    - Google Gemini (`gemini-3.5-flash`, `gemini-3.1-pro-preview`, `gemini-flash-latest`).
    - OpenAI (`gpt-4o`, `gpt-4o-mini`, `o3-mini`).
    - Live endpoint connection tester.
    - Robust academic offline fallback so the app is always fully functional.

13. **Global AI Copilot & Command Center**:
    - Persistent natural language command bar routing actions ("Start 25m Focus", "Show weak chapters", "Revise cards", "Take Kinematics Test").
