package edu.muiv.univapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.login.Login
import edu.muiv.univapp.schedule.Schedule
import edu.muiv.univapp.user.User
import java.util.concurrent.Executors

class UnivRepository private constructor(context: Context){

    companion object {
        private const val DATABASE_NAME = "univ-database"
        private var INSTANCE: UnivRepository? = null

        fun initialize(ctx: Context) {
            if (INSTANCE == null) {
                INSTANCE = UnivRepository(ctx)
            }
        }

        fun get(): UnivRepository {
            return INSTANCE ?: throw IllegalStateException("UnivRepository must be initialized")
        }
    }

    private val database: UnivDatabase = Room.databaseBuilder(
        context.applicationContext,
        UnivDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val univDAO = database.univDAO()
    private val executor = Executors.newSingleThreadExecutor()

    fun getUser(user: Login): LiveData<User>? = univDAO.getUser(user.username, user.password)

    fun getSchedule(): LiveData<List<Schedule>> = univDAO.getSchedule()

    fun getScheduleByDay(): LiveData<List<Schedule>> = univDAO.getScheduleByDay()

    fun getScheduleByDate(date: String): LiveData<List<Schedule>> = univDAO.getScheduleByDate(date)

    fun addSchedule(schedule: Schedule) {
        executor.execute {
            univDAO.addSchedule(schedule)
        }
    }

    fun addUser(user: User) {
        executor.execute {
            univDAO.addUser(user)
        }
    }
}