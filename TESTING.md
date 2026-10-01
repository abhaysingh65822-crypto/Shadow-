# StudyForge Testing & Verification

## Test Architecture
- **Local Unit & Robolectric Tests**:
  - `ExampleRobolectricTest.kt`: Tests Android context string retrieval (`StudyForge`), SM-2 spaced repetition interval logic, and scoring arithmetic without requiring a physical device.
- **Compilation Verification**:
  - Run via `compile_applet` which invokes Gradle `assembleDebug`.
- **End-to-End CUJ Flows**:
  1. *Flow A*: Question answered incorrectly in Question Bank → automatically logged in Mistake Book → flagged as weak topic.
  2. *Flow B*: Test completed in Test Engine → scored with negative marking → test attempt recorded → high/low performance advice generated → revision scheduled.
  3. *Flow C*: Focus session completed in Pomodoro → adds study minutes to chapter and profile → updates streak and awards XP.
  4. *Flow D*: Flashcard rated Good in Spaced Repetition → interval increased via SM-2 equation → due date updated.
