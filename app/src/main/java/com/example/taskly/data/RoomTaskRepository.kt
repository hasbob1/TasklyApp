package com.example.taskly.data

import com.example.taskly.data.local.SubjectDao
import com.example.taskly.data.local.TaskDao
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import kotlinx.coroutines.flow.Flow

class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> = taskDao.observeTasks()
    override fun observeSubjects(): Flow<List<Subject>> = subjectDao.observeSubjects()

    override suspend fun getTask(id: Long): Task? = taskDao.getTask(id)
    override suspend fun saveTask(task: Task): Long = taskDao.saveTask(task)
    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    override suspend fun setCompleted(taskId: Long, completed: Boolean) =
        taskDao.setCompleted(taskId, completed)

    override suspend fun addSubject(subject: Subject): Long = subjectDao.addSubject(subject)
    override suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    override suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)
}
