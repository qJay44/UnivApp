package edu.muiv.univapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.muiv.univapp.schedule.Schedule
import edu.muiv.univapp.user.User

@Database(entities = [ Schedule::class, User::class ], version = 1, exportSchema = false)
@TypeConverters(UnivTypeConverters::class)
abstract class UnivDatabase : RoomDatabase() {
    abstract fun univDAO(): UnivDAO
}