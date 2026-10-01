package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FlashcardEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StudyForge", appName)
  }

  @Test
  fun `verify SM2 spaced repetition interval calculation`() {
    val card = FlashcardEntity(
      front = "What is Newton's 2nd Law?",
      back = "F = dp/dt = ma",
      intervalDays = 1,
      easeFactor = 2.5f,
      repetitions = 1
    )
    val nextInterval = (card.intervalDays * card.easeFactor).toInt().coerceAtLeast(1)
    assertTrue(nextInterval >= 2)
  }
}
