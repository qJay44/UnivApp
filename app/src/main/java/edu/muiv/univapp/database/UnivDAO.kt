package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.muiv.univapp.Schedule

@Dao
interface UnivDAO {
    @Query("SELECT * FROM schedule")
    fun getSchedule(): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule GROUP BY date")
    fun getScheduleByDay(): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE date=:date")
    fun getScheduleByDate(date: String): LiveData<List<Schedule>>

    @Insert
    fun addSchedule(schedule: Schedule)
}