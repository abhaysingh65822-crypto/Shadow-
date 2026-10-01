package com.example.data

object SeedData {

    val defaultProfile = UserProfileEntity(
        id = 1,
        currentStreak = 4,
        longestStreak = 9,
        totalXp = 1240,
        level = 5,
        dailyTargetMinutes = 120,
        examTargetName = "National STEM & Engineering Entrance",
        examTargetDaysRemaining = 38,
        lastActiveDate = System.currentTimeMillis()
    )

    val subjects = listOf(
        SubjectEntity(id = 1, name = "Physics", code = "PHY-101", colorHex = "#38BDF8", iconName = "bolt", totalChapters = 5, completedChapters = 2),
        SubjectEntity(id = 2, name = "Chemistry", code = "CHM-102", colorHex = "#10B981", iconName = "science", totalChapters = 4, completedChapters = 1),
        SubjectEntity(id = 3, name = "Mathematics", code = "MTH-103", colorHex = "#818CF8", iconName = "functions", totalChapters = 6, completedChapters = 3),
        SubjectEntity(id = 4, name = "Computer Science", code = "CSE-104", colorHex = "#F59E0B", iconName = "terminal", totalChapters = 4, completedChapters = 2)
    )

    val chapters = listOf(
        // Physics
        ChapterEntity(id = 1, subjectId = 1, title = "Motion in 1D & Kinematics", orderIndex = 1, completionPercent = 85, masteryLevel = 82, confidenceScore = 80, timeSpentMinutes = 190),
        ChapterEntity(id = 2, subjectId = 1, title = "Laws of Motion & Friction", orderIndex = 2, completionPercent = 90, masteryLevel = 78, confidenceScore = 75, timeSpentMinutes = 240),
        ChapterEntity(id = 3, subjectId = 1, title = "Work, Energy & Power", orderIndex = 3, completionPercent = 60, masteryLevel = 58, confidenceScore = 60, timeSpentMinutes = 110),
        ChapterEntity(id = 4, subjectId = 1, title = "Rotational Mechanics & Torque", orderIndex = 4, completionPercent = 35, masteryLevel = 32, confidenceScore = 40, timeSpentMinutes = 75),
        ChapterEntity(id = 5, subjectId = 1, title = "Thermodynamics & Kinetic Theory", orderIndex = 5, completionPercent = 15, masteryLevel = 20, confidenceScore = 30, timeSpentMinutes = 40),

        // Chemistry
        ChapterEntity(id = 6, subjectId = 2, title = "Chemical Bonding & Molecular Structure", orderIndex = 1, completionPercent = 80, masteryLevel = 75, confidenceScore = 70, timeSpentMinutes = 160),
        ChapterEntity(id = 7, subjectId = 2, title = "Chemical Equilibrium & Le Chatelier", orderIndex = 2, completionPercent = 45, masteryLevel = 42, confidenceScore = 45, timeSpentMinutes = 95),
        ChapterEntity(id = 8, subjectId = 2, title = "Thermodynamics & Enthalpy", orderIndex = 3, completionPercent = 25, masteryLevel = 30, confidenceScore = 35, timeSpentMinutes = 50),
        ChapterEntity(id = 9, subjectId = 2, title = "Organic Reaction Mechanisms", orderIndex = 4, completionPercent = 10, masteryLevel = 15, confidenceScore = 20, timeSpentMinutes = 30),

        // Mathematics
        ChapterEntity(id = 10, subjectId = 3, title = "Differential Calculus & Limits", orderIndex = 1, completionPercent = 95, masteryLevel = 90, confidenceScore = 88, timeSpentMinutes = 280),
        ChapterEntity(id = 11, subjectId = 3, title = "Integral Calculus & Areas", orderIndex = 2, completionPercent = 70, masteryLevel = 65, confidenceScore = 65, timeSpentMinutes = 180),
        ChapterEntity(id = 12, subjectId = 3, title = "Vectors & 3D Geometry", orderIndex = 3, completionPercent = 50, masteryLevel = 48, confidenceScore = 55, timeSpentMinutes = 120),
        ChapterEntity(id = 13, subjectId = 3, title = "Matrices & Determinants", orderIndex = 4, completionPercent = 85, masteryLevel = 88, confidenceScore = 90, timeSpentMinutes = 150),
        ChapterEntity(id = 14, subjectId = 3, title = "Probability & Bayes Theorem", orderIndex = 5, completionPercent = 40, masteryLevel = 38, confidenceScore = 40, timeSpentMinutes = 80),
        ChapterEntity(id = 15, subjectId = 3, title = "Differential Equations", orderIndex = 6, completionPercent = 20, masteryLevel = 25, confidenceScore = 30, timeSpentMinutes = 45),

        // CS
        ChapterEntity(id = 16, subjectId = 4, title = "Data Structures & Arrays", orderIndex = 1, completionPercent = 90, masteryLevel = 88, confidenceScore = 90, timeSpentMinutes = 220),
        ChapterEntity(id = 17, subjectId = 4, title = "Binary Trees & BST", orderIndex = 2, completionPercent = 65, masteryLevel = 60, confidenceScore = 65, timeSpentMinutes = 140),
        ChapterEntity(id = 18, subjectId = 4, title = "Graph Theory & Traversal", orderIndex = 3, completionPercent = 30, masteryLevel = 35, confidenceScore = 35, timeSpentMinutes = 60),
        ChapterEntity(id = 19, subjectId = 4, title = "Dynamic Programming & Memoization", orderIndex = 4, completionPercent = 15, masteryLevel = 18, confidenceScore = 20, timeSpentMinutes = 45)
    )

    val topics = listOf(
        TopicEntity(id = 1, chapterId = 1, title = "Equations of Motion under Constant Acceleration", subtopicsJson = "[\"Derivation of v=u+at\", \"Displacement in nth second\", \"v^2-u^2=2as\"]", isCompleted = true, masteryScore = 88, accuracyRate = 85, revisionStatus = "MASTERED", difficultyLevel = "EASY", isWeak = false),
        TopicEntity(id = 2, chapterId = 1, title = "Relative Velocity & River Swimmer Problems", subtopicsJson = "[\"Frame of Reference\", \"Shortest Path vs Minimum Time\", \"Rain Man Problems\"]", isCompleted = true, masteryScore = 65, accuracyRate = 60, revisionStatus = "DUE", difficultyLevel = "HARD", isWeak = true),
        TopicEntity(id = 3, chapterId = 2, title = "Newton's 2nd Law & Free Body Diagrams", subtopicsJson = "[\"Internal vs External Forces\", \"Pulley Block Systems\", \"Wedge Constraints\"]", isCompleted = true, masteryScore = 80, accuracyRate = 78, revisionStatus = "MASTERED", difficultyLevel = "MEDIUM", isWeak = false),
        TopicEntity(id = 4, chapterId = 2, title = "Friction & Rolling Resistance", subtopicsJson = "[\"Static vs Kinetic Friction\", \"Angle of Repose\", \"Two Block Friction Problems\"]", isCompleted = false, masteryScore = 52, accuracyRate = 48, revisionStatus = "DUE", difficultyLevel = "HARD", isWeak = true),
        TopicEntity(id = 5, chapterId = 4, title = "Moment of Inertia Theorems", subtopicsJson = "[\"Parallel Axis Theorem\", \"Perpendicular Axis Theorem\", \"Standard Body Integrations\"]", isCompleted = false, masteryScore = 35, accuracyRate = 30, revisionStatus = "DUE", difficultyLevel = "HARD", isWeak = true),
        TopicEntity(id = 6, chapterId = 7, title = "Le Chatelier Principle & Pressure Shifts", subtopicsJson = "[\"Effect of Volume & Pressure\", \"Inert Gas Addition\", \"Temperature Dependence\"]", isCompleted = false, masteryScore = 45, accuracyRate = 42, revisionStatus = "DUE", difficultyLevel = "MEDIUM", isWeak = true),
        TopicEntity(id = 7, chapterId = 10, title = "L'Hopital's Rule & Indeterminate Forms", subtopicsJson = "[\"0/0 and inf/inf forms\", \"1^inf conversion\", \"Standard Limits\"]", isCompleted = true, masteryScore = 92, accuracyRate = 95, revisionStatus = "MASTERED", difficultyLevel = "EASY", isWeak = false),
        TopicEntity(id = 8, chapterId = 11, title = "Definite Integrals by Substitution & By Parts", subtopicsJson = "[\"King's Property\", \"Leibniz Integral Rule\", \"Periodic Integrals\"]", isCompleted = true, masteryScore = 68, accuracyRate = 65, revisionStatus = "DUE", difficultyLevel = "HARD", isWeak = false)
    )

    val questions = listOf(
        QuestionEntity(
            id = 1,
            subjectId = 1,
            chapterId = 1,
            topicId = 1,
            questionText = "A car starts from rest and accelerates uniformly at 2 m/s² for 10 seconds. What is the total distance covered?",
            optionsJson = "[\"50 m\", \"100 m\", \"200 m\", \"150 m\"]",
            correctAnswer = "100 m",
            explanation = "Using s = ut + (1/2)at² with u = 0, a = 2 m/s², t = 10 s: s = 0 + (1/2)(2)(10²) = 100 meters.",
            hint = "Recall the second kinematic equation s = ut + 0.5at².",
            difficulty = "EASY",
            isPyq = true,
            year = "2023"
        ),
        QuestionEntity(
            id = 2,
            subjectId = 1,
            chapterId = 2,
            topicId = 4,
            questionText = "A block of mass 5 kg lies on a rough horizontal surface with coefficient of static friction μ_s = 0.4. A horizontal force of 15 N is applied. What is the frictional force exerted on the block? (Take g = 9.8 m/s²)",
            optionsJson = "[\"19.6 N\", \"15.0 N\", \"0 N\", \"25.0 N\"]",
            correctAnswer = "15.0 N",
            explanation = "Maximum static friction f_max = μ_s * N = 0.4 * 5 * 9.8 = 19.6 N. Since the applied force (15 N) is less than f_max, the block remains at rest and static friction self-adjusts to exactly balance the applied force: f = 15 N.",
            hint = "Static friction is a self-adjusting force up to its threshold value.",
            difficulty = "HARD",
            isPyq = true,
            year = "2022"
        ),
        QuestionEntity(
            id = 3,
            subjectId = 1,
            chapterId = 4,
            topicId = 5,
            questionText = "What is the radius of gyration of a solid sphere of mass M and radius R about its diametric axis?",
            optionsJson = "[\"R * √(2/5)\", \"R * √(1/2)\", \"R * √(3/5)\", \"R * (2/5)\"]",
            correctAnswer = "R * √(2/5)",
            explanation = "Moment of inertia of solid sphere I = (2/5)MR². By definition I = MK², therefore K² = (2/5)R², giving K = R * √(2/5).",
            hint = "Express moment of inertia in terms of M*K².",
            difficulty = "MEDIUM",
            isPyq = false,
            year = "2024"
        ),
        QuestionEntity(
            id = 4,
            subjectId = 2,
            chapterId = 7,
            topicId = 6,
            questionText = "For the exothermic Haber process N₂(g) + 3H₂(g) ⇌ 2NH₃(g) (ΔH < 0), which condition shifts equilibrium forward to produce more ammonia?",
            optionsJson = "[\"Increasing temperature\", \"Decreasing pressure\", \"Increasing pressure at optimal temperature\", \"Removing N₂ gas\"]",
            correctAnswer = "Increasing pressure at optimal temperature",
            explanation = "Forward reaction decreases moles of gas (4 moles reactants → 2 moles product). By Le Chatelier's principle, increasing pressure shifts equilibrium towards fewer moles (forward). Lower temperature favors exothermic reactions, but optimal temp (~450°C) is kept for kinetic catalyst rate.",
            hint = "Count total gaseous moles on both sides.",
            difficulty = "MEDIUM",
            isPyq = true,
            year = "2023"
        ),
        QuestionEntity(
            id = 5,
            subjectId = 3,
            chapterId = 10,
            topicId = 7,
            questionText = "Evaluate the limit as x approaches 0 of (sin(3x) - 3x) / x³.",
            optionsJson = "[\"0\", \"-4.5\", \"-1.5\", \"-3\"]",
            correctAnswer = "-4.5",
            explanation = "Using Taylor expansion: sin(3x) = 3x - (3x)³/3! + O(x⁵) = 3x - 27x³/6 = 3x - 4.5x³. Thus (sin(3x)-3x)/x³ = -4.5. Alternatively applying L'Hopital's Rule 3 times gives -27/6 = -4.5.",
            hint = "Use Taylor series of sin(u) = u - u³/6.",
            difficulty = "HARD",
            isPyq = true,
            year = "2024"
        ),
        QuestionEntity(
            id = 6,
            subjectId = 4,
            chapterId = 16,
            topicId = 8,
            questionText = "What is the worst-case time complexity of searching for an element in an unsorted array of size N versus a balanced Binary Search Tree?",
            optionsJson = "[\"O(N) and O(log N)\", \"O(1) and O(log N)\", \"O(N) and O(N)\", \"O(log N) and O(1)\"]",
            correctAnswer = "O(N) and O(log N)",
            explanation = "Unsorted array search requires scanning elements linearly in O(N). A balanced BST (like AVL or Red-Black) guarantees height O(log N), so search is O(log N).",
            hint = "Binary search tree divides search space in half at each node.",
            difficulty = "EASY",
            isPyq = false,
            year = "2024"
        )
    )

    val mistakes = listOf(
        MistakeEntity(
            id = 1,
            questionId = 2,
            questionText = "Block on rough surface with μ_s = 0.4, mass 5kg, applied force 15N. What is the frictional force?",
            subjectName = "Physics",
            topicName = "Friction & Rolling Resistance",
            userAnswer = "19.6 N",
            correctAnswer = "15.0 N",
            explanation = "Common careless mistake: applying max friction f = μ*N directly without verifying whether applied force exceeds threshold. Since applied force 15N < 19.6N, friction is 15N.",
            category = "CONCEPTUAL",
            repeatCount = 2,
            reviewStatus = "NEEDS_PRACTICE"
        ),
        MistakeEntity(
            id = 2,
            questionId = 4,
            questionText = "Exothermic Haber process: effect of temperature and pressure shifts.",
            subjectName = "Chemistry",
            topicName = "Le Chatelier Principle & Pressure Shifts",
            userAnswer = "Increasing temperature",
            correctAnswer = "Increasing pressure at optimal temperature",
            explanation = "Confused exothermic temperature shift (increasing T shifts backward) with kinetic rate boost. In exothermic equilibrium, temperature rise decreases equilibrium yield.",
            category = "MEMORY",
            repeatCount = 1,
            reviewStatus = "NEEDS_PRACTICE"
        )
    )

    val decks = listOf(
        DeckEntity(id = 1, name = "Physics High-Yield Formulas", subjectId = 1, description = "Mechanics, Work-Energy, Kinematics formulas and constraints", cardCount = 3),
        DeckEntity(id = 2, name = "Chemistry Key Reagents", subjectId = 2, description = "Equilibrium constants, Le Chatelier shifts, bonding geometries", cardCount = 2),
        DeckEntity(id = 3, name = "Calculus Master Theorems", subjectId = 3, description = "Standard limits, integration by parts tricks, Leibniz formula", cardCount = 2)
    )

    val flashcards = listOf(
        FlashcardEntity(
            id = 1,
            deckId = 1,
            subjectId = 1,
            front = "What is the work-energy theorem for a particle?",
            back = "Work done by all forces (conservative + non-conservative + external) equals the change in kinetic energy: W_total = ΔK.",
            cardType = "STANDARD",
            intervalDays = 1,
            easeFactor = 2.5f,
            repetitions = 2,
            lapses = 0,
            dueDate = System.currentTimeMillis() - 10000
        ),
        FlashcardEntity(
            id = 2,
            deckId = 1,
            subjectId = 1,
            front = "Parallel Axis Theorem Formula",
            back = "I = I_cm + M * d², where d is the perpendicular distance between the center of mass axis and the parallel axis.",
            cardType = "FORMULA",
            intervalDays = 3,
            easeFactor = 2.6f,
            repetitions = 4,
            lapses = 0,
            dueDate = System.currentTimeMillis() - 50000
        ),
        FlashcardEntity(
            id = 3,
            deckId = 2,
            subjectId = 2,
            front = "Effect of adding an inert gas at CONSTANT VOLUME to a gaseous equilibrium?",
            back = "NO EFFECT on equilibrium position because partial pressures and concentrations of reacting species remain unchanged.",
            cardType = "STANDARD",
            intervalDays = 1,
            easeFactor = 2.4f,
            repetitions = 1,
            lapses = 1,
            dueDate = System.currentTimeMillis() + 86400000
        ),
        FlashcardEntity(
            id = 4,
            deckId = 3,
            subjectId = 3,
            front = "Leibniz Rule for differentiating under the integral sign: d/dx ∫[u(x) to v(x)] f(t) dt",
            back = "f(v(x)) * v'(x) - f(u(x)) * u'(x)",
            cardType = "FORMULA",
            intervalDays = 4,
            easeFactor = 2.7f,
            repetitions = 5,
            lapses = 0,
            dueDate = System.currentTimeMillis() - 20000
        )
    )

    val plannerTasks = listOf(
        PlannerTaskEntity(
            id = 1,
            title = "Solve 15 Friction & Rolling Problems",
            taskType = "PRACTICE",
            subjectName = "Physics",
            chapterName = "Laws of Motion & Friction",
            scheduledDate = System.currentTimeMillis(),
            startTime = "09:30 AM",
            durationMinutes = 45,
            isCompleted = false,
            isMissed = false,
            priority = "HIGH"
        ),
        PlannerTaskEntity(
            id = 2,
            title = "Revise Chemistry Equilibrium Due Cards",
            taskType = "REVISION",
            subjectName = "Chemistry",
            chapterName = "Chemical Equilibrium",
            scheduledDate = System.currentTimeMillis(),
            startTime = "02:00 PM",
            durationMinutes = 30,
            isCompleted = false,
            isMissed = false,
            priority = "HIGH"
        ),
        PlannerTaskEntity(
            id = 3,
            title = "Attempt Calculus Mock Mini-Test",
            taskType = "TEST",
            subjectName = "Mathematics",
            chapterName = "Differential Calculus",
            scheduledDate = System.currentTimeMillis() + 86400000,
            startTime = "11:00 AM",
            durationMinutes = 40,
            isCompleted = false,
            isMissed = false,
            priority = "MEDIUM"
        )
    )

    val notes = listOf(
        NoteEntity(
            id = 1,
            title = "Kinematics & Constant Acceleration Cheatsheet",
            contentMarkdown = """
                # Kinematics Summary
                
                ### Core Equations
                * v = u + at
                * s = ut + 0.5 * a * t^2
                * v^2 - u^2 = 2as
                * Distance in nth second: s_n = u + (a/2)(2n - 1)
                
                ### Common Traps
                1. Equations are only valid when acceleration a is constant in magnitude and direction.
                2. For vertical motion under gravity, choose an upward or downward positive convention consistently.
            """.trimIndent(),
            subjectName = "Physics",
            chapterName = "Motion in 1D & Kinematics",
            folder = "Mechanics",
            tags = "[\"kinematics\", \"formulas\", \"high-yield\"]",
            isPinned = true,
            isFavorite = true
        ),
        NoteEntity(
            id = 2,
            title = "Le Chatelier's Principle & Equilibrium Shifts",
            contentMarkdown = """
                # Chemical Equilibrium Quick Guide
                
                ### Factors Affecting Position
                * **Concentration**: Adding reactant shifts forward; adding product shifts backward.
                * **Pressure**: Increasing pressure shifts equilibrium toward side with fewer moles of gas.
                * **Inert Gas**:
                  * At **constant volume**: No shift.
                  * At **constant pressure**: Shifts toward larger number of moles.
                * **Temperature**: Endothermic reactions favor higher T; exothermic reactions favor lower T.
            """.trimIndent(),
            subjectName = "Chemistry",
            chapterName = "Chemical Equilibrium",
            folder = "Physical Chemistry",
            tags = "[\"equilibrium\", \"le-chatelier\", \"exam-favorite\"]",
            isPinned = false,
            isFavorite = true
        )
    )

    val documents = listOf(
        StudyDocumentEntity(
            id = 1,
            title = "Physics Classical Mechanics Handbook (Ch. 1-3 Excerpt)",
            fileType = "DOCUMENT",
            pageCount = 14,
            summary = "Comprehensive lecture notes on Newton's laws, friction thresholds, constraint equations, and work-energy balance.",
            contentExtract = "Newton's laws form the cornerstone of non-relativistic dynamics. When two bodies interact on a rough contact surface, the static frictional force matches the external shear component until reaching the threshold value f_s,max = μ_s * N. Once sliding begins, kinetic friction f_k = μ_k * N opposes the relative velocity.",
            tags = "[\"mechanics\", \"friction\", \"newton-laws\"]"
        ),
        StudyDocumentEntity(
            id = 2,
            title = "Calculus Integration Formulas & Special Limits",
            fileType = "DOCUMENT",
            pageCount = 8,
            summary = "Summary of essential definite integral properties, King's rule, and Leibniz integral rule.",
            contentExtract = "The King's property states that the integral of f(x) from a to b is equal to the integral of f(a + b - x) from a to b. This is exceptionally powerful for evaluating trigonometric quotients where the denominator remains invariant under substitution.",
            tags = "[\"calculus\", \"limits\", \"derivatives\"]"
        )
    )

    val notifications = listOf(
        NotificationItemEntity(
            id = 1,
            title = "Spaced Repetition Due",
            message = "You have 3 flashcards ready for revision in Physics & Calculus.",
            type = "REVISION_DUE",
            priority = "HIGH",
            isRead = false,
            actionRoute = "flashcards"
        ),
        NotificationItemEntity(
            id = 2,
            title = "Weak Topic Warning",
            message = "Accuracy in 'Friction & Rolling' dropped below 50%. Practice targeted questions recommended.",
            type = "WEAK_TOPIC",
            priority = "HIGH",
            isRead = false,
            actionRoute = "questions"
        ),
        NotificationItemEntity(
            id = 3,
            title = "Streak Maintained!",
            message = "4-day study streak active! Keep studying today to reach your 5-day milestone.",
            type = "STREAK",
            priority = "NORMAL",
            isRead = true,
            actionRoute = "dashboard"
        )
    )

    val tests = listOf(
        TestEntity(
            id = 1,
            title = "Physics Kinematics & Friction Benchmark Test",
            testType = "CHAPTER_TEST",
            durationMinutes = 20,
            totalMarks = 24,
            totalQuestions = 6,
            subjectFilter = "Physics",
            chapterFilter = "Laws of Motion & Friction"
        ),
        TestEntity(
            id = 2,
            title = "STEM Combined Mixed Diagnostic Exam",
            testType = "MIXED_TEST",
            durationMinutes = 35,
            totalMarks = 40,
            totalQuestions = 10,
            subjectFilter = "All Subjects",
            chapterFilter = "High Yield Chapters"
        )
    )

    val testAttempts = listOf(
        TestAttemptEntity(
            id = 1,
            testId = 1,
            testTitle = "Physics Kinematics & Friction Benchmark Test",
            score = 16,
            maxScore = 24,
            accuracy = 67,
            completedAt = System.currentTimeMillis() - 86400000,
            durationSec = 980,
            attemptedCount = 6,
            skippedCount = 0,
            incorrectCount = 2,
            analysisNotes = "Strong grasp of 1D kinematics. Slipping errors observed in static friction limit calculation."
        )
    )

    val studySessions = listOf(
        StudySessionEntity(
            id = 1,
            subjectId = 1,
            subjectName = "Physics",
            chapterName = "Motion in 1D & Kinematics",
            durationMinutes = 50,
            sessionType = "POMODORO",
            notes = "Solved 12 Kinematics motion problems and reviewed relative velocity graph formulas.",
            timestamp = System.currentTimeMillis() - 86400000,
            xpEarned = 100
        ),
        StudySessionEntity(
            id = 2,
            subjectId = 3,
            subjectName = "Mathematics",
            chapterName = "Differential Calculus",
            durationMinutes = 45,
            sessionType = "DEEP_WORK",
            notes = "Mastered L'Hopital's indeterminate powers and practiced polynomial expansions.",
            timestamp = System.currentTimeMillis() - 43200000,
            xpEarned = 90
        )
    )
}
