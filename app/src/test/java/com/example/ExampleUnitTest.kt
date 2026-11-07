package com.example

import com.example.data.prayer.EgyptianPrayerCalculator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPrayerCalculationAlexandria() {
        // Test date calculation for Alexandria, Egypt
        val times = EgyptianPrayerCalculator.calculateTimes("2026-11-07")
        assertNotNull(times.fajr)
        assertNotNull(times.dhuhr)
        assertNotNull(times.asr)
        assertNotNull(times.maghrib)
        assertNotNull(times.isha)

        assertTrue(times.fajr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.dhuhr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.asr.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.maghrib.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(times.isha.matches(Regex("\\d{2}:\\d{2}")))
    }
}
