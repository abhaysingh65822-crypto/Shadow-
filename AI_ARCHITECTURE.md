# StudyForge AI Architecture & Multi-Provider System

## Supported Providers & Models

1. **Google Gemini**:
   - `gemini-3.5-flash`: Fast, high-efficiency model for everyday Q&A, flashcard creation, and summaries.
   - `gemini-3.1-pro-preview`: Deep reasoning model for complex STEM derivations, physics proofs, and competitive questions.
   - `gemini-flash-latest`: Automatically resolves to the latest production release of Gemini Flash.

2. **OpenAI**:
   - `gpt-4o`: Flagship multi-modal model.
   - `gpt-4o-mini`: Lightweight, low-latency reasoning engine.
   - `o3-mini`: Advanced STEM and coding reasoning model.

3. **Academic Fallback Engine**:
   - When offline or when API keys are not supplied, StudyForge's built-in academic expert engine handles student prompts with detailed mathematical formulas, derivations, Socratic questioning, and structured markdown.

## Dynamic Context Injection

All AI requests automatically receive student telemetry context:
- Target examination name and days remaining.
- Current active weak concepts and low accuracy topics.
- Active study streak and daily target minutes.
- Selected chapter and topic context.

## 10 Specialized Bots

1. `AI_TUTOR`: Socratic teacher.
2. `AI_PLANNER`: Daily and weekly schedule optimizer.
3. `AI_EXAMINER`: Test creator and rubric grader.
4. `AI_REVISION_COACH`: Spaced repetition drillmaster.
5. `AI_NOTES_ASSISTANT`: Concept summarizer.
6. `AI_FLASHCARD_GENERATOR`: High-yield active recall card generator.
7. `AI_CODING_TUTOR`: Algorithm, data structure, and complexity guide.
8. `AI_MATHEMATICS_SOLVER`: Step-by-step calculus proofs and algebra derivations.
9. `AI_PHYSICS_TUTOR`: Mechanics, free-body diagrams, and work-energy.
10. `AI_CHEMISTRY_TUTOR`: Reaction mechanisms, equilibrium shifts, and thermodynamics.
