package com.example.taskly.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.taskly.data.model.Priority
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var subjectDao: SubjectDao

    @Before
    fun setUp() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        taskDao = db.taskDao()
        subjectDao = db.subjectDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun sampleTask(subjectId: Long) = Task(
        title = "Lab report",
        subjectId = subjectId,
        dueDate = LocalDate.of(2026, 11, 5),
        priority = Priority.HIGH,
        dueTime = LocalTime.of(10, 30),
    )

    @Test
    fun saveTask_roundTripsAllFields() {
        runBlocking {
            val subjectId = subjectDao.addSubject(Subject(name = "Physics", colorArgb = 123))
            val id = taskDao.saveTask(sampleTask(subjectId))
            val loaded = taskDao.getTask(id)
            assertEquals(sampleTask(subjectId).copy(id = id), loaded)
        }
    }

    @Test
    fun saveTask_withExistingId_updatesInsteadOfInserting() {
        runBlocking {
            val subjectId = subjectDao.addSubject(Subject(name = "Physics", colorArgb = 1))
            val id = taskDao.saveTask(sampleTask(subjectId))
            taskDao.saveTask(sampleTask(subjectId).copy(id = id, title = "Edited"))
            val all = taskDao.observeTasks().first()
            assertEquals(1, all.size)
            assertEquals("Edited", all.first().title)
        }
    }

    @Test
    fun setCompleted_changesOnlyTheFlag() {
        runBlocking {
            val subjectId = subjectDao.addSubject(Subject(name = "Physics", colorArgb = 1))
            val id = taskDao.saveTask(sampleTask(subjectId))
            taskDao.setCompleted(id, true)
            assertTrue(taskDao.getTask(id)!!.isCompleted)
        }
    }

    @Test
    fun deleteTask_removesIt() {
        runBlocking {
            val subjectId = subjectDao.addSubject(Subject(name = "Physics", colorArgb = 1))
            val id = taskDao.saveTask(sampleTask(subjectId))
            taskDao.deleteTask(taskDao.getTask(id)!!)
            assertNull(taskDao.getTask(id))
        }
    }

    @Test
    fun deleteSubject_cascadesToItsTasks() {
        runBlocking {
            val subjectId = subjectDao.addSubject(Subject(name = "Physics", colorArgb = 1))
            val otherId = subjectDao.addSubject(Subject(name = "English", colorArgb = 2))
            taskDao.saveTask(sampleTask(subjectId))
            taskDao.saveTask(sampleTask(otherId))
            subjectDao.deleteSubject(subjectDao.observeSubjects().first().first { it.id == subjectId })
            val remaining = taskDao.observeTasks().first()
            assertEquals(1, remaining.size)
            assertEquals(otherId, remaining.first().subjectId)
        }
    }
}
