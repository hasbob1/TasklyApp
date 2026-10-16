package com.example.taskly.data

import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// temporary repository with sample data, replaced by the Room one
class InMemoryTaskRepository : TaskRepository {
    private val subjects = MutableStateFlow(
        listOf(
            Subject(1, "Mobile Computing", 0xFF4F46E5.toInt()),
            Subject(2, "Calculus", 0xFFE11D48.toInt()),
            Subject(3, "Physics", 0xFF0D9488.toInt()),
            Subject(4, "English", 0xFFD97706.toInt()),
        ),
    )

    private val tasks = MutableStateFlow(seedTasks(LocalDate.now()))

    private var nextTaskId = 100L
    private var nextSubjectId = 100L

    override fun observeTasks(): Flow<List<Task>> = tasks
    override fun observeSubjects(): Flow<List<Subject>> = subjects

    override suspend fun getTask(id: Long): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun saveTask(task: Task): Long {
        val saved = if (task.id == 0L) task.copy(id = nextTaskId++) else task
        tasks.update { list -> list.filterNot { it.id == saved.id } + saved }
        return saved.id
    }

    override suspend fun deleteTask(task: Task) {
        tasks.update { list -> list.filterNot { it.id == task.id } }
    }

    override suspend fun setCompleted(taskId: Long, completed: Boolean) {
        tasks.update { list ->
            list.map { if (it.id == taskId) it.copy(isCompleted = completed) else it }
        }
    }

    override suspend fun addSubject(subject: Subject): Long {
        val saved = subject.copy(id = nextSubjectId++)
        subjects.update { it + saved }
        return saved.id
    }

    override suspend fun updateSubject(subject: Subject) {
        subjects.update { list -> list.map { if (it.id == subject.id) subject else it } }
    }

    override suspend fun deleteSubject(subject: Subject) {
        subjects.update { list -> list.filterNot { it.id == subject.id } }
        tasks.update { list -> list.filterNot { it.subjectId == subject.id } }
    }

    private fun seedTasks(today: LocalDate) = listOf(
        Task(1, "Finish Taskly Phase 1", "Compose UI, README and the 2-page abstract", 1, today, Priority.HIGH, dueTime = LocalTime.of(23, 0)),
        Task(2, "Calculus problem set 4", "Chapters 5 and 6", 2, today, Priority.MEDIUM, dueTime = LocalTime.of(22, 0)),
        Task(3, "Physics lab report", "Include the error analysis section", 3, today.plusDays(1), Priority.HIGH, dueTime = LocalTime.of(10, 30)),
        Task(4, "Read the English article", "", 4, today.plusDays(2), Priority.LOW, dueTime = LocalTime.of(18, 0)),
        Task(5, "Mobile Computing midterm revision", "Lectures 1 to 6", 1, today.plusDays(3), Priority.HIGH, dueTime = LocalTime.of(9, 0)),
        Task(6, "Submit calculus quiz", "", 2, today.minusDays(1), Priority.MEDIUM, dueTime = LocalTime.of(12, 0)),
        Task(7, "Vocabulary flashcards", "", 4, today, Priority.LOW, isCompleted = true, dueTime = LocalTime.of(20, 0)),
    )
}
