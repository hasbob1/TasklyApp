package com.example.taskly.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskly.data.TaskRepository
import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import com.example.taskly.util.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TitleError { EMPTY, TOO_LONG }

data class AddEditUiState(
    val title: String = "",
    val description: String = "",
    val subjectId: Long? = null,
    val dueDate: LocalDate = LocalDate.now(),
    val priority: Priority = Priority.MEDIUM,
    val dueTime: LocalTime = defaultDueTime(),
    val remindBeforeMinutes: Int = AddEditViewModel.DEFAULT_REMINDER_MINUTES,
    val deadlineInPast: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val subjectTaskCounts: Map<Long, Int> = emptyMap(),
    val titleError: TitleError? = null,
    val subjectError: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
)

private fun defaultDueTime(): LocalTime {
    val now = LocalTime.now()
    return if (now.hour >= 23) LocalTime.of(23, 59) else now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
}

sealed interface AddEditEvent {
    data object Saved : AddEditEvent
    data object Deleted : AddEditEvent
    data object TaskNotFound : AddEditEvent
}

class AddEditViewModel(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
    private val taskId: Long? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AddEditUiState(isEditing = taskId != null, isLoading = taskId != null),
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddEditEvent>(Channel.BUFFERED)
    val events: Flow<AddEditEvent> = _events.receiveAsFlow()

    private var existingTask: Task? = null

    init {
        repository.observeSubjects()
            .onEach { subjects -> _uiState.update { it.copy(subjects = subjects) } }
            .launchIn(viewModelScope)
        repository.observeTasks()
            .onEach { tasks ->
                val counts = tasks.groupingBy { it.subjectId }.eachCount()
                _uiState.update { it.copy(subjectTaskCounts = counts) }
            }
            .launchIn(viewModelScope)
        if (taskId != null) loadTask(taskId)
    }

    private fun loadTask(id: Long) {
        viewModelScope.launch {
            val task = repository.getTask(id)
            if (task == null) {
                _events.send(AddEditEvent.TaskNotFound)
                return@launch
            }
            existingTask = task
            _uiState.update {
                it.copy(
                    title = task.title,
                    description = task.description,
                    subjectId = task.subjectId,
                    dueDate = task.dueDate,
                    priority = task.priority,
                    dueTime = task.dueTime,
                    remindBeforeMinutes = task.remindBeforeMinutes,
                    isLoading = false,
                )
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value.take(MAX_TITLE_LENGTH), titleError = null)
    }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(MAX_DESCRIPTION_LENGTH))
    }

    fun onSubjectSelected(subjectId: Long) = _uiState.update {
        it.copy(subjectId = subjectId, subjectError = false)
    }

    fun onDateSelected(date: LocalDate) = _uiState.update { it.copy(dueDate = date, deadlineInPast = false) }

    fun onTimeSelected(time: LocalTime) = _uiState.update { it.copy(dueTime = time, deadlineInPast = false) }

    fun onReminderSelected(minutesBefore: Int) = _uiState.update { it.copy(remindBeforeMinutes = minutesBefore) }

    fun onPrioritySelected(priority: Priority) = _uiState.update { it.copy(priority = priority) }

    fun onCreateSubject(name: String, colorArgb: Int) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = repository.addSubject(Subject(name = trimmed, colorArgb = colorArgb))
            _uiState.update { it.copy(subjectId = id, subjectError = false) }
        }
    }

    fun onUpdateSubject(subject: Subject, name: String, colorArgb: Int) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val updated = subject.copy(name = trimmed, colorArgb = colorArgb)
            repository.updateSubject(updated)
            repository.observeTasks().first()
                .filter { it.subjectId == subject.id && !it.isCompleted }
                .forEach { reminderScheduler.schedule(it, trimmed) }
        }
    }

    fun onDeleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.observeTasks().first()
                .filter { it.subjectId == subject.id }
                .forEach { reminderScheduler.cancel(it.id) }
            repository.deleteSubject(subject)
            if (_uiState.value.subjectId == subject.id) {
                _uiState.update { it.copy(subjectId = null) }
            }
            if (existingTask?.subjectId == subject.id) {
                _events.send(AddEditEvent.Deleted)
            }
        }
    }

    fun validateForm(): Boolean {
        val state = _uiState.value
        val titleError = validateTitle(state.title)
        val subjectMissing = state.subjectId == null
        val inPast = !state.isEditing && state.dueDate.atTime(state.dueTime).isBefore(LocalDateTime.now())
        _uiState.update {
            it.copy(titleError = titleError, subjectError = subjectMissing, deadlineInPast = inPast)
        }
        return titleError == null && !subjectMissing && !inPast
    }

    fun onSave() {
        if (_uiState.value.isSaving || !validateForm()) return
        val state = _uiState.value
        val subjectId = state.subjectId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val draft = Task(
                id = existingTask?.id ?: 0L,
                title = state.title.trim(),
                description = state.description.trim(),
                subjectId = subjectId,
                dueDate = state.dueDate,
                priority = state.priority,
                isCompleted = existingTask?.isCompleted ?: false,
                dueTime = state.dueTime,
                remindBeforeMinutes = state.remindBeforeMinutes,
            )
            val savedId = repository.saveTask(draft)
            reminderScheduler.schedule(
                task = draft.copy(id = savedId),
                subjectName = state.subjects.firstOrNull { it.id == subjectId }?.name,
            )
            _events.send(AddEditEvent.Saved)
        }
    }

    fun onDelete() {
        val task = existingTask ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            reminderScheduler.cancel(task.id)
            _events.send(AddEditEvent.Deleted)
        }
    }

    private fun validateTitle(title: String): TitleError? = when {
        title.isBlank() -> TitleError.EMPTY
        title.trim().length > MAX_TITLE_LENGTH -> TitleError.TOO_LONG
        else -> null
    }

    companion object {
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 500
        const val DEFAULT_REMINDER_MINUTES = 60

        fun factory(
            repository: TaskRepository,
            reminderScheduler: ReminderScheduler,
            taskId: Long? = null,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddEditViewModel(repository, reminderScheduler, taskId) }
        }
    }
}