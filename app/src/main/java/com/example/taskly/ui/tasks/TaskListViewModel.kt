package com.example.taskly.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskly.data.TaskRepository
import com.example.taskly.data.model.Task
import com.example.taskly.util.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TaskListEvent {
    data class TaskDeleted(val title: String) : TaskListEvent
}

class TaskListViewModel(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
    private val nowProvider: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {
    private val filter = MutableStateFlow(TaskListFilter())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<TaskListUiState> = combine(
        repository.observeTasks(),
        repository.observeSubjects(),
        filter,
        _query,
    ) { tasks, subjects, currentFilter, currentQuery ->
        buildTaskListState(tasks, subjects, currentFilter.copy(query = currentQuery), nowProvider())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TaskListUiState(),
    )

    private val _events = Channel<TaskListEvent>(Channel.BUFFERED)
    val events: Flow<TaskListEvent> = _events.receiveAsFlow()

    private var lastDeleted: Task? = null

    fun onQueryChange(value: String) {
        _query.value = value.take(MAX_QUERY_LENGTH)
    }

    fun onModeChange(mode: ListMode) = filter.update { it.copy(mode = mode) }

    fun onDateSelected(date: LocalDate?) = filter.update { it.copy(selectedDate = date) }

    fun onSubjectSelected(subjectId: Long?) = filter.update { it.copy(selectedSubjectId = subjectId) }

    fun onClearFilter() = filter.update { it.copy(selectedDate = null, selectedSubjectId = null) }

    fun onToggleComplete(task: Task) {
        viewModelScope.launch {
            val completed = !task.isCompleted
            repository.setCompleted(task.id, completed)
            if (completed) reminderScheduler.cancel(task.id) else rescheduleReminder(task)
        }
    }

    fun onDelete(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            reminderScheduler.cancel(task.id)
            lastDeleted = task
            _events.send(TaskListEvent.TaskDeleted(task.title))
        }
    }

    fun onUndoDelete() {
        val task = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            repository.saveTask(task)
            rescheduleReminder(task)
        }
    }

    private fun rescheduleReminder(task: Task) {
        val subjectName = uiState.value.subjectsById[task.subjectId]?.name
        reminderScheduler.schedule(task.copy(isCompleted = false), subjectName)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val MAX_QUERY_LENGTH = 60

        fun factory(repository: TaskRepository, reminderScheduler: ReminderScheduler): ViewModelProvider.Factory =
            viewModelFactory { initializer { TaskListViewModel(repository, reminderScheduler) } }
    }
}
