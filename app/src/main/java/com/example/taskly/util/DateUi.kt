package com.example.taskly.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.taskly.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

fun LocalDate.formatFull(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))

fun LocalTime.formatShort(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

fun LocalDate.shortWeekday(locale: Locale = Locale.getDefault()): String =
    dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

@Composable
fun LocalDate.toRelativeLabel(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> stringResource(R.string.common_today)
    today.plusDays(1) -> stringResource(R.string.common_tomorrow)
    today.minusDays(1) -> stringResource(R.string.common_yesterday)
    else -> format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
}
