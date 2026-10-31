package com.example.taskly.ui.dashboard

import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import java.time.LocalDateTime

private const val UPCOMING_LIMIT = 5

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val remaining: Int = 0,
    val overdue: Int = 0,
) {
    val progress: Float get() = if (total == 0) 0f else completed.toFloat() / total
}

data class UpcomingTask(val task: Task, val subject: Subject?)

data class SubjectStat(val subject: Subject, val total: Int, val completed: Int) {
    val progress: Float get() = if (total == 0) 0f else completed.toFloat() / total
}

data class DashboardUiState(
    val stats: TaskStats = TaskStats(),
    val upcoming: List<UpcomingTask> = emptyList(),
    val subjectStats: List<SubjectStat> = emptyList(),
    val darkMode: Boolean? = null,
)

fun calculateStats(tasks: List<Task>, now: LocalDateTime): TaskStats = TaskStats(
    total = tasks.size,

    completed = tasks.count { it.isCompleted },
    remaining = tasks.count { !it.isCompleted },
    overdue = tasks.count { !it.isCompleted && it.dueDateTime.isBefore(now) },
)

fun buildDashboardState(
    tasks: List<Task>,
    subjects: List<Subject>,
    darkMode: Boolean?,
    now: LocalDateTime,
): DashboardUiState {
    val subjectsById = subjects.associateBy { it.id }
    val upcoming = tasks
        .filter { !it.isCompleted && !it.dueDateTime.isBefore(now) }
        .sortedBy { it.dueDateTime }
        .take(UPCOMING_LIMIT)
        .map { UpcomingTask(it, subjectsById[it.subjectId]) }
    val subjectStats = subjects
        .map { subject ->
            val own = tasks.filter { it.subjectId == subject.id }
            SubjectStat(subject, total = own.size, completed = own.count { it.isCompleted })
        }
        .filter { it.total > 0 }

    return DashboardUiState(
        stats = calculateStats(tasks, now),
        upcoming = upcoming,
        subjectStats = subjectStats,
        darkMode = darkMode,
    )
}