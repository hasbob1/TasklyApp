package com.example.taskly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Family = FontFamily.Default

private fun style(weight: FontWeight, size: Int, line: Int, spacing: Double = 0.0) = TextStyle(
    fontFamily = Family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = spacing.sp,
)

val TasklyTypography = Typography(
    displaySmall = style(FontWeight.Bold, 36, 44, -0.5),
    headlineLarge = style(FontWeight.Bold, 32, 40, -0.3),
    headlineMedium = style(FontWeight.Bold, 28, 36, -0.2),
    headlineSmall = style(FontWeight.SemiBold, 24, 32),
    titleLarge = style(FontWeight.SemiBold, 20, 28),
    titleMedium = style(FontWeight.SemiBold, 16, 24, 0.1),
    titleSmall = style(FontWeight.SemiBold, 14, 20, 0.1),
    bodyLarge = style(FontWeight.Normal, 16, 24, 0.2),
    bodyMedium = style(FontWeight.Normal, 14, 20, 0.2),
    bodySmall = style(FontWeight.Normal, 12, 16, 0.3),
    labelLarge = style(FontWeight.Medium, 14, 20, 0.1),
    labelMedium = style(FontWeight.Medium, 12, 16, 0.4),
    labelSmall = style(FontWeight.Medium, 11, 16, 0.4),
)
