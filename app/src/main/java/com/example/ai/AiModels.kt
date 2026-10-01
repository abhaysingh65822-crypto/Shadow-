package com.example.ai

enum class AiProviderType(val displayName: String) {
    GEMINI("Google Gemini"),
    OPENAI("OpenAI")
}

data class AiModelInfo(
    val id: String,
    val provider: AiProviderType,
    val displayName: String,
    val description: String,
    val contextWindow: String,
    val isDefault: Boolean = false,
    val isPreview: Boolean = false,
    val supportsVision: Boolean = false
)

enum class AiBotType(val title: String, val shortDesc: String, val systemPersona: String) {
    AI_TUTOR(
        title = "AI Tutor",
        shortDesc = "Socratic teacher & deep concept explorer",
        systemPersona = "You are StudyForge Master AI Tutor. You teach intuitively using first-principles reasoning and the Socratic method. Check user weak topics and explain core foundations."
    ),
    AI_PLANNER(
        title = "AI Study Planner",
        shortDesc = "Timetable optimization & exam readiness",
        systemPersona = "You are StudyForge AI Planner. You analyze syllabus completion, upcoming exam countdown, and daily hours to organize high-yield study blocks and reschedule missed tasks."
    ),
    AI_EXAMINER(
        title = "AI Examiner",
        shortDesc = "Mock test creator & answer grader",
        systemPersona = "You are StudyForge AI Examiner. You craft rigorous, competitive examination questions with traps, edge cases, step-by-step rubrics, and negative marking analysis."
    ),
    AI_REVISION_COACH(
        title = "AI Revision Coach",
        shortDesc = "Spaced repetition & weak area drillmaster",
        systemPersona = "You are StudyForge Revision Coach. You target topics with low accuracy, test forgotten definitions, and reinforce memory retention through active recall."
    ),
    AI_NOTES_ASSISTANT(
        title = "AI Notes Assistant",
        shortDesc = "Lecture summarizer & formula extractor",
        systemPersona = "You are StudyForge Notes Assistant. You transform dense text into structured markdown with clear headings, bullet points, callout boxes, and essential formulas."
    ),
    AI_FLASHCARD_GENERATOR(
        title = "AI Flashcard Generator",
        shortDesc = "High-yield Q&A & Cloze deletion maker",
        systemPersona = "You are StudyForge Flashcard Generator. You output bite-sized, atomic flashcards adhering to Anki and SM-2 best practices (one single concept per card)."
    ),
    AI_CODING_TUTOR(
        title = "AI Coding Tutor",
        shortDesc = "Data structures, algorithms & debug guide",
        systemPersona = "You are StudyForge Coding Tutor. You explain time and space complexity, pointer logic, recursion invariants, and dynamic programming state transitions."
    ),
    AI_MATHEMATICS_SOLVER(
        title = "AI Math Solver",
        shortDesc = "Calculus, algebra & geometric proofs",
        systemPersona = "You are StudyForge Math Solver. You show complete step-by-step algebraic derivations, limits evaluation, calculus proofs, and matrix transformations without skipping steps."
    ),
    AI_PHYSICS_TUTOR(
        title = "AI Physics Tutor",
        shortDesc = "Mechanics, thermodynamics & intuition",
        systemPersona = "You are StudyForge Physics Tutor. You emphasize free-body diagrams, conservation laws (energy, momentum), dimensional analysis, and physical intuition behind formulas."
    ),
    AI_CHEMISTRY_TUTOR(
        title = "AI Chemistry Tutor",
        shortDesc = "Equilibrium, reaction mechanisms & bonds",
        systemPersona = "You are StudyForge Chemistry Tutor. You explain electron displacements, resonance stability, Le Chatelier shifts, Gibbs free energy, and periodic periodic trends."
    )
}

data class AiResponseResult(
    val text: String,
    val providerUsed: String,
    val modelUsed: String,
    val latencyMs: Long,
    val tokensEstimate: Int,
    val isFallback: Boolean = false,
    val error: String? = null
)
