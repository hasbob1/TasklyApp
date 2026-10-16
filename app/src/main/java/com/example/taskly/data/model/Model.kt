package com.example.taskly.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Priority { LOW, MEDIUM, HIGH }

data class Subject(
    val id: Long = 0L,
    val name: String,
    val colorArgb: Int,
)

data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val subjectId: Long,
    val dueDate: LocalDate,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,

    val dueTime: LocalTime = LocalTime.of(9, 0),

    val remindBeforeMinutes: Int = 60,
) {
    val dueDateTime: LocalDateTime get() = dueDate.atTime(dueTime)
}
