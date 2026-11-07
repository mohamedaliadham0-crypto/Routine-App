package com.example.data.prayer

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

/**
 * Egyptian General Authority of Survey method:
 * - Alexandria, Egypt: Lat: 31.2001, Lon: 29.9187
 * - Fajr angle: 19.5 deg
 * - Isha angle: 17.5 deg
 * - Maghrib angle: -0.833 deg
 * - Asr shadow factor: 1 (Standard Shafi'i/Hanbali/Maliki used in Egypt)
 * - Africa/Cairo timezone with DST awareness
 */
data class SunPrayerTimes(
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class PrayerInfo(
    val id: String,
    val name: String,
    val time: String,
    val status: String // "done", "missed", or ""
)

object EgyptianPrayerCalculator {
    const val DEFAULT_LAT = 31.2001
    const val DEFAULT_LON = 29.9187

    val PRAYERS_LIST = listOf(
        "fajr" to "Fajr",
        "dhuhr" to "Dhuhr",
        "asr" to "Asr",
        "maghrib" to "Maghrib",
        "isha" to "Isha"
    )

    fun calculateTimes(
        dateStr: String, // YYYY-MM-DD
        lat: Double = DEFAULT_LAT,
        lon: Double = DEFAULT_LON
    ): SunPrayerTimes {
        val parts = dateStr.split("-").map { it.toInt() }
        val year = parts[0]
        val month = parts[1]
        val day = parts[2]

        val localDate = LocalDate.of(year, month, day)
        val cairoZone = try {
            ZoneId.of("Africa/Cairo")
        } catch (e: Exception) {
            ZoneId.of("GMT+2")
        }
        val zonedDateTime = ZonedDateTime.of(localDate.atTime(12, 0), cairoZone)
        val offsetHours = zonedDateTime.offset.totalSeconds / 3600.0

        val dayOfYear = localDate.dayOfYear
        val g = 2.0 * Math.PI / 365.0 * (dayOfYear - 1)
        val eq = 229.18 * (0.000075 + 0.001868 * cos(g) - 0.032077 * sin(g) - 0.014615 * cos(2.0 * g) - 0.040849 * sin(2.0 * g))
        val dec = 0.006918 - 0.399912 * cos(g) + 0.070257 * sin(g) - 0.006758 * cos(2.0 * g) + 0.000907 * sin(2.0 * g) - 0.002697 * cos(3.0 * g) + 0.00148 * sin(3.0 * g)

        val noon = 720.0 - 4.0 * lon - eq + offsetHours * 60.0
        val la = lat * Math.PI / 180.0

        val ha = { angleDeg: Double ->
            val r = angleDeg * Math.PI / 180.0
            val x = (sin(r) - sin(la) * sin(dec)) / (cos(la) * cos(dec))
            val clamped = x.coerceIn(-1.0, 1.0)
            acos(clamped) * 229.1831180523293
        }

        val asrAlt = 180.0 / Math.PI * atan(1.0 / (1.0 + tan(abs(la - dec))))

        val fajrMin = noon - ha(-19.5)
        val dhuhrMin = noon
        val asrMin = noon + ha(asrAlt)
        val maghribMin = noon + ha(-0.833)
        val ishaMin = noon + ha(-17.5)

        return SunPrayerTimes(
            fajr = formatMinutes(fajrMin),
            dhuhr = formatMinutes(dhuhrMin),
            asr = formatMinutes(asrMin),
            maghrib = formatMinutes(maghribMin),
            isha = formatMinutes(ishaMin)
        )
    }

    private fun formatMinutes(minutes: Double): String {
        var m = ((minutes % 1440.0) + 1440.0) % 1440.0
        var h = (m / 60.0).toInt()
        var i = (m % 60.0).roundToInt()
        if (i == 60) {
            h = (h + 1) % 24
            i = 0
        }
        return String.format("%02d:%02d", h, i)
    }
}
