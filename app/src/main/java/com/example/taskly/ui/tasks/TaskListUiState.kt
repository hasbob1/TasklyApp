package com.example.taskly.ui.tasks

import androidx.compose.runtime.Immutable
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import java.time.LocalDate
import java.time.LocalDateTime

enum class ListMode { BY_DAY, BY_SUBJECT }

sealed interface SectionHeaderModel {
    data object Overdue : SectionHeaderModel
    data class Day(val date: LocalDate) : SectionHeaderModel
    data class SubjectGroup(val subject: Subject) : SectionHeaderModel

    val key: String
        get() = when (this) {
            Overdue -> "header_overdue"
            is Day -> "header_day_$date"
            is SubjectGroup -> "header_subject_${subject.id}"
        }
}

@Immutable
data class TaskSection(
    val header: SectionHeaderModel,
    val tasks: List<Task>,
)

@Immutable
data class TaskListUiState(
    val isLoading: Boolean = true,
    val mode: ListMode = ListMode.BY_DAY,
    val selectedDate: LocalDate? = null,
    val selectedSubjectId: Long? = null,
    val query: String = "",
    val today: LocalDate = LocalDate.now(),
    val sections: List<TaskSection> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val daysWithOpenTasks: Set<LocalDate> = emptySet(),
    val remainingCount: Int = 0,
    val completedCount: Int = 0,
    val hasAnyTasks: Boolean = false,
) {
    val subjectsById: Map<Long, Subject> by lazy { subjects.associateBy { it.id } }
}

internal data class TaskListFilter(
    val mode: ListMode = ListMode.BY_DAY,
    val selectedDate: LocalDate? = null,
    val selectedSubjectId: Long? = null,

    val query: String = "",
)

internal fun buildTaskListState(
    tasks: List<Task>,
    subjects: List<Subject>,
    filter: TaskListFilter,
    now: LocalDateTime,
): TaskListUiState {
    val today = now.toLocalDate()
    val selectedSubjectId = filter.selectedSubjectId?.takeIf { id -> subjects.any { it.id == id } }
    val searchTerm = filter.query.trim()
    val searched = if (searchTerm.isEmpty()) tasks else tasks.filter { it.title.contains(searchTerm, ignoreCase = true) }
    val sections = when (filter.mode) {
        ListMode.BY_DAY -> groupByDay(searched, filter.selectedDate, now)
        ListMode.BY_SUBJECT -> groupBySubject(searched, subjects, selectedSubjectId)
    }
    return TaskListUiState(
        isLoading = false,
        mode = filter.mode,
        selectedDate = filter.selectedDate,
        selectedSubjectId = selectedSubjectId,
        query = filter.query,
        today = today,
        sections = sections,
        subjects = subjects.sortedBy { it.name.lowercase() },
        daysWithOpenTasks = tasks.filterNot { it.isCompleted }.mapTo(mutableSetOf()) { it.dueDate },
        remainingCount = tasks.count { !it.isCompleted },
        completedCount = tasks.count { it.isCompleted },
        hasAnyTasks = tasks.isNotEmpty(),
    )
}

private val byOpenThenTimeThenPriority =
    compareBy<Task>({ it.isCompleted }, { it.dueTime }, { -it.priority.ordinal }, { it.title.lowercase() })

private val byDateTimeThenPriority =
    compareBy<Task>({ it.dueDate }, { it.dueTime }, { -it.priority.ordinal })

private val byOpenThenDateTimeThenPriority =
    compareBy<Task>({ it.isCompleted }, { it.dueDate }, { it.dueTime }, { -it.priority.ordinal })

private fun groupByDay(tasks: List<Task>, selectedDate: LocalDate?, now: LocalDateTime): List<TaskSection> {
    if (selectedDate != null) {
        val dayTasks = tasks.filter { it.dueDate == selectedDate }.sortedWith(byOpenThenTimeThenPriority)
        return if (dayTasks.isEmpty()) emptyList() else listOf(TaskSection(SectionHeaderModel.Day(selectedDate), dayTasks))
    }
    val (overdue, rest) = tasks.partition { !it.isCompleted && it.dueDateTime.isBefore(now) }
    return buildList {
        if (overdue.isNotEmpty()) {
            add(TaskSection(SectionHeaderModel.Overdue, overdue.sortedWith(byDateTimeThenPriority)))
        }
        rest.groupBy { it.dueDate }
            .toSortedMap()
            .forEach { (date, group) ->
                add(TaskSection(SectionHeaderModel.Day(date), group.sortedWith(byOpenThenTimeThenPriority)))
            }
    }
}

private fun groupBySubject(tasks: List<Task>, subjects: List<Subject>, selectedId: Long?): List<TaskSection> {
    val visible = if (selectedId == null) tasks else tasks.filter { it.subjectId == selectedId }
    val bySubject = visible.groupBy { it.subjectId }
    return subjects
        .sortedBy { it.name.lowercase() }
        .mapNotNull { subject ->
            bySubject[subject.id]?.let { group ->
                TaskSection(SectionHeaderModel.SubjectGroup(subject), group.sortedWith(byOpenThenDateTimeThenPriority))
            }
        }
}
