package com.example.taskly.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Priority { LOW, MEDIUM, HIGH }

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorArgb: Int,
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("subjectId")],
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String = "",
    val subjectId: Long,
    val dueDate: LocalDate,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,

    val dueTime: LocalTime = LocalTime.of(9, 0),

    val remindBeforeMinutes: Int = 60,
) {
    @get:Ignore
    val dueDateTime: LocalDateTime get() = dueDate.atTime(dueTime)
}
