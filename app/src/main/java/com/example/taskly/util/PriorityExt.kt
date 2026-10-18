package com.example.taskly.util

import androidx.annotation.StringRes
import com.example.taskly.R
import com.example.taskly.data.model.Priority

@StringRes
fun Priority.labelRes(): Int = when (this) {
    Priority.LOW -> R.string.priority_low
    Priority.MEDIUM -> R.string.priority_medium
    Priority.HIGH -> R.string.priority_high
}
