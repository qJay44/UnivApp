package edu.muiv.univapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.muiv.univapp.ui.schedule.Schedule
import edu.muiv.univapp.user.Student
import edu.muiv.univapp.user.Teacher

@Database(entities = [ Schedule::class, Student::class, Teacher::class ], version = 1, exportSchema = false)
@TypeConverters(UnivTypeConverters::class)
abstract class UnivDatabase : RoomDatabase() {
    abstract fun univDAO(): UnivDAO
}