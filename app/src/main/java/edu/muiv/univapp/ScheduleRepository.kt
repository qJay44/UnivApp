package edu.muiv.univapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.database.UnivDatabase
import java.util.concurrent.Executors

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

    private val database: UnivDatabase = Room.databaseBuilder(
        context.applicationContext,
        UnivDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val univDAO = database.univDAO()
    private val executor = Executors.newSingleThreadExecutor()

    fun getSchedule(): LiveData<List<Schedule>> = univDAO.getSchedule()

    fun getScheduleByDay(): LiveData<List<Schedule>> = univDAO.getScheduleByDay()

    fun getScheduleByDate(date: String): LiveData<List<Schedule>> = univDAO.getScheduleByDate(date)

    fun addSchedule(schedule: Schedule) {
        executor.execute {
            univDAO.addSchedule(schedule)
        }
    }
}