package edu.muiv.univapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.muiv.univapp.Schedule

@Database(entities = [ Schedule::class ], version = 1, exportSchema = false)
@TypeConverters(ScheduleTypeConverters::class)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleDAO(): ScheduleDAO
}