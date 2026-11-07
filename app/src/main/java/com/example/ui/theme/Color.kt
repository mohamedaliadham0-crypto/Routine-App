package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Routine StudyOS Brand Colors
val BrandAccent = Color(0xFF7C3AED)      // Vibrant Purple
val BrandPink = Color(0xFFFF3D9A)        // Neon Pink
val BrandCyan = Color(0xFF00B8D9)        // Bright Cyan
val BrandOrange = Color(0xFFFF8A00)      // Warm Orange
val BrandGreen = Color(0xFF16C784)       // Emerald Green
val BrandRed = Color(0xFFFF4D6D)         // Rose Red
val BrandYellow = Color(0xFFFFB800)      // Amber Yellow

// Subject Palette (matching the HTML PAL)
val SubjectPalette = listOf(
    Color(0xFF7C3AED), // Purple
    Color(0xFFFF3D9A), // Pink
    Color(0xFF00B8D9), // Cyan
    Color(0xFFFF8A00), // Orange
    Color(0xFF22C55E), // Green
    Color(0xFF3B82F6), // Blue
    Color(0xFFE11D48)  // Crimson
)

fun getSubjectColor(subject: String?): Color {
    if (subject.isNullOrBlank()) return SubjectPalette[0]
    var h = 0
    for (c in subject) {
        h = (h * 31 + c.code) % 97
    }
    return SubjectPalette[Math.abs(h % SubjectPalette.size)]
}

// Gradients
val PrimaryGradient = Brush.linearGradient(listOf(BrandAccent, BrandPink))
val CyanBlueGradient = Brush.linearGradient(listOf(Color(0xFF00C2A8), Color(0xFF3B82F6)))
val OrangeGoldGradient = Brush.linearGradient(listOf(Color(0xFFFF7A00), Color(0xFFFFC93C)))
val BluePurpleGradient = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
val SuccessGradient = Brush.linearGradient(listOf(BrandGreen, BrandCyan))

// Dark Theme Colors
val DarkBg = Color(0xFF160F33)
val DarkSurface = Color(0xFF241952)
val DarkSurfaceVariant = Color(0xFF2E2266)
val DarkLine = Color(0xFF43348C)
val DarkText = Color(0xFFFFFFFF)
val DarkMuted = Color(0xFFB8A9E6)

// Light Theme Colors
val LightBg = Color(0xFFFFF5FD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF6EFFF)
val LightLine = Color(0xFFEADCFF)
val LightText = Color(0xFF2A1B4D)
val LightMuted = Color(0xFF7A6A9C)
