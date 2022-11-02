package edu.muiv.univapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.database.ScheduleDatabase
import java.util.*

class ScheduleRepository private constructor(context: Context){

    companion object {
        private const val DATABASE_NAME = "schedule-database"
        private var INSTANCE: ScheduleRepository? = null

        fun initialize(ctx: Context) {
            if (INSTANCE == null) {
                INSTANCE = ScheduleRepository(ctx)
            }
        }

        fun get(): ScheduleRepository {
            return INSTANCE ?: throw IllegalStateException("ScheduleRepository must be initialized")
        }
    }

    private val database: ScheduleDatabase = Room.databaseBuilder(
        context.applicationContext,
        ScheduleDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val scheduleDAO = database.scheduleDAO()

    fun getSchedule(): LiveData<List<Schedule>> = scheduleDAO.getSchedule()

    fun getScheduleByDay() = scheduleDAO.getScheduleByDay()

    fun getScheduleByDate(date: Date) = scheduleDAO.getScheduleByDate(date)
}