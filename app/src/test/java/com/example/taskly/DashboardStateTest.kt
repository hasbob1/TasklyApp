package com.example.taskly

import com.example.taskly.data.model.Task
import com.example.taskly.ui.dashboard.buildDashboardState
import com.example.taskly.ui.dashboard.calculateStats
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStateTest {

    private val now = LocalDateTime.of(2026, 10, 10, 12, 0)
    private val today: LocalDate = now.toLocalDate()

    private fun task(id: Long, due: LocalDate = today, done: Boolean = false) = Task(
        id = id,
        title = "Task $id",
        subjectId = 1,
        dueDate = due,
        isCompleted = done,
        dueTime = LocalTime.of(18, 0),
    )

    @Test
    fun countsRemainingCompletedAndOverdue() {
        val tasks = listOf(
            task(1, done = true),
            task(2, due = today.minusDays(1)),
            task(3, due = today.plusDays(2)),
        )


        val stats = calculateStats(tasks, now)

        assertEquals(3, stats.total)
        assertEquals(1, stats.completed)
        assertEquals(2, stats.remaining)
        assertEquals(1, stats.overdue)
    }

    @Test
    fun upcomingSkipsOverdueAndCompletedAndIsSortedByDate() {
        val tasks = listOf(
            task(1, due = today.plusDays(3)),
            task(2, due = today.minusDays(1)),
            task(3, done = true),
            task(4, due = today),
        )

        val upcoming = buildDashboardState(tasks, emptyList(), null, now).upcoming

        assertEquals(listOf(4L, 1L), upcoming.map { it.task.id })
    }

    @Test
    fun progressIsZeroWhenThereAreNoTasks() {
        assertEquals(0f, calculateStats(emptyList(), now).progress, 0f)
    }
}