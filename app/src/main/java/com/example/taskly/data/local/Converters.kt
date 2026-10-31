package com.example.taskly.data.local

import androidx.room.TypeConverter
import com.example.taskly.data.model.Priority
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun toLocalTime(seconds: Int?): LocalTime? =
        seconds?.let { LocalTime.ofSecondOfDay(it.toLong()) }

    @TypeConverter
    fun fromPriority(priority: Priority?): String? = priority?.name

    @TypeConverter
    fun toPriority(value: String?): Priority? = value?.let { Priority.valueOf(it) }
}
