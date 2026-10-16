package com.example.taskly.data

import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeSubjects(): Flow<List<Subject>>

    suspend fun getTask(id: Long): Task?
    suspend fun saveTask(task: Task): Long
    suspend fun deleteTask(task: Task)
    suspend fun setCompleted(taskId: Long, completed: Boolean)

    suspend fun addSubject(subject: Subject): Long
    suspend fun updateSubject(subject: Subject)

    suspend fun deleteSubject(subject: Subject)
}
