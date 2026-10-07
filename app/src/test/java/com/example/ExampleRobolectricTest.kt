package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DwellAction
import com.example.model.GestureType
import com.example.model.UserProfile
import com.example.tracking.DwellDetector
import com.example.tracking.MotionFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AuraMotion", appName)
    }

    @Test
    fun `motion filter suppresses micro tremors within deadzone`() {
        val filter = MotionFilter(baseSmoothing = 0.2f, deadZoneRadius = 0.05f)
        filter.reset(0.5f, 0.5f)

        // Micro-tremor displacement of 0.02 (smaller than deadzone 0.05)
        val (filteredX, filteredY) = filter.filter(0.51f, 0.51f)

        // Filtered position should remain anchored at initial resting position
        assertEquals(0.5f, filteredX, 0.001f)
        assertEquals(0.5f, filteredY, 0.001f)
    }

    @Test
    fun `dwell detector progresses and triggers after dwell duration`() {
        val dwellDetector = DwellDetector(dwellDurationMs = 800L, toleranceRadius = 0.05f)
        dwellDetector.reset()

        val baseTime = 10000L

        // Initial hover start
        val (p0, trig0) = dwellDetector.update(0.5f, 0.5f, baseTime)
        assertEquals(0f, p0, 0.01f)
        assertFalse(trig0)

        // Halfway through dwell (400ms elapsed)
        val (pHalf, trigHalf) = dwellDetector.update(0.51f, 0.51f, baseTime + 400L)
        assertTrue(pHalf in 0.45f..0.55f)
        assertFalse(trigHalf)

        // Completed dwell (850ms elapsed)
        val (pFull, trigFull) = dwellDetector.update(0.51f, 0.51f, baseTime + 850L)
        assertEquals(1f, pFull, 0.01f)
        assertTrue(trigFull)
    }

    @Test
    fun `dwell detector resets when moving outside tolerance radius`() {
        val dwellDetector = DwellDetector(dwellDurationMs = 800L, toleranceRadius = 0.05f)
        dwellDetector.reset()

        val baseTime = 10000L
        dwellDetector.update(0.5f, 0.5f, baseTime)
        dwellDetector.update(0.51f, 0.51f, baseTime + 400L)

        // Jump far outside tolerance (e.g. to 0.8f, 0.8f)
        val (pReset, trigReset) = dwellDetector.update(0.8f, 0.8f, baseTime + 500L)
        assertEquals(0f, pReset, 0.01f)
        assertFalse(trigReset)
    }

    @Test
    fun `preset profiles have valid configurations`() {
        val tremor = UserProfile.TREMOR_RELIEF
        assertTrue(tremor.smoothingFactor < UserProfile.BALANCED.smoothingFactor)
        assertTrue(tremor.deadZoneRadius > UserProfile.BALANCED.deadZoneRadius)

        val lowRange = UserProfile.LIMITED_RANGE_OF_MOTION
        assertTrue(lowRange.sensitivityX > UserProfile.BALANCED.sensitivityX)
    }
}
