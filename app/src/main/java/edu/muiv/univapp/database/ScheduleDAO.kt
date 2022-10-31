package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import edu.muiv.univapp.Schedule

@Dao
interface ScheduleDAO {
    @Query("SELECT * FROM schedule")
    fun getSchedule(): LiveData<List<Schedule>>
}