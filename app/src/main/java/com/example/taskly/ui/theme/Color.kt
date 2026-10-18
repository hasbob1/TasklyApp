package com.example.taskly.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF3B1B7A),
    background = Color(0xFFF6F7FD),
    onBackground = Color(0xFF14152B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14152B),
    surfaceVariant = Color(0xFFECEEF9),
    onSurfaceVariant = Color(0xFF5B5F7A),
    outline = Color(0xFF8A8EA8),
    outlineVariant = Color(0xFFDADDEE),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    inverseSurface = Color(0xFF24253F),
    inverseOnSurface = Color(0xFFF1F2FB),
    inversePrimary = Color(0xFFA5B4FC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9FAFF),
    surfaceContainer = Color(0xFFF1F2FB),
    surfaceContainerHigh = Color(0xFFEBECF7),
    surfaceContainerHighest = Color(0xFFE4E6F3),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFC4B5FD),
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = Color(0xFF0E0F1F),
    onBackground = Color(0xFFE7E8F6),
    surface = Color(0xFF16172B),
    onSurface = Color(0xFFE7E8F6),
    surfaceVariant = Color(0xFF24264A),
    onSurfaceVariant = Color(0xFFA9ADCB),
    outline = Color(0xFF7E82A3),
    outlineVariant = Color(0xFF34365A),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    inverseSurface = Color(0xFFE7E8F6),
    inverseOnSurface = Color(0xFF24253F),
    inversePrimary = Color(0xFF4F46E5),
    surfaceContainerLowest = Color(0xFF0A0B18),
    surfaceContainerLow = Color(0xFF121325),
    surfaceContainer = Color(0xFF1B1C32),
    surfaceContainerHigh = Color(0xFF24253D),
    surfaceContainerHighest = Color(0xFF2E2F4A),
)

@Immutable
data class TasklyColors(
    val priorityHigh: Color,
    val priorityHighContainer: Color,
    val priorityMedium: Color,
    val priorityMediumContainer: Color,
    val priorityLow: Color,
    val priorityLowContainer: Color,
)

internal val LightTasklyColors = TasklyColors(
    priorityHigh = Color(0xFFE11D48),
    priorityHighContainer = Color(0xFFFFE4E9),
    priorityMedium = Color(0xFFD97706),
    priorityMediumContainer = Color(0xFFFEF3C7),
    priorityLow = Color(0xFF059669),
    priorityLowContainer = Color(0xFFD1FAE5),
)

internal val DarkTasklyColors = TasklyColors(
    priorityHigh = Color(0xFFFB7185),
    priorityHighContainer = Color(0xFF4C1D2A),
    priorityMedium = Color(0xFFFBBF24),
    priorityMediumContainer = Color(0xFF4A3410),
    priorityLow = Color(0xFF34D399),
    priorityLowContainer = Color(0xFF0F3D2E),
)

internal val LocalTasklyColors = staticCompositionLocalOf<TasklyColors> {
    error("TasklyTheme is missing: wrap your content in TasklyTheme { ... }")
}

val MaterialTheme.taskly: TasklyColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTasklyColors.current

val SubjectPalette: List<Color> = listOf(
    Color(0xFF4F46E5),
    Color(0xFFE11D48),
    Color(0xFF0D9488),
    Color(0xFFD97706),
    Color(0xFF7C3AED),
    Color(0xFF2563EB),
    Color(0xFFDB2777),
    Color(0xFF65A30D),
)
