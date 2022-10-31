package edu.muiv.univapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.database.ScheduleDatabase
import java.util.concurrent.Executors

class ScheduleRepository private constructor(ctx: Context){

    companion object {
        private const val TAG = "ScheduleRepository"
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
        ctx.applicationContext,
        ScheduleDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val scheduleDAO = database.scheduleDAO()

    fun getSchedule(): LiveData<List<Schedule>> = scheduleDAO.getSchedule()
}