package com.example.taskly.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.taskly.data.TaskRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    repository: TaskRepository,
    private val nowProvider: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeTasks(),
        repository.observeSubjects(),
    ) { tasks, subjects ->
        buildDashboardState(tasks, subjects, nowProvider())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DashboardUiState(),
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            viewModelFactory { initializer { DashboardViewModel(repository) } }
    }
}