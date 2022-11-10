package edu.muiv.univapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.login.Login
import edu.muiv.univapp.login.LoginResult
import edu.muiv.univapp.schedule.Schedule
import edu.muiv.univapp.user.*
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

    fun getUser(login: Login): LiveData<LoginResult> = univDAO.getUser(login.username, login.password)
    fun getTeacherWithSchedules(): LiveData<TeacherWithSchedules> = univDAO.getTeacherWithSchedules()

    fun getSchedule(): LiveData<List<Schedule>> = univDAO.getSchedule()
    fun getScheduleByDay(group: String): LiveData<List<Schedule>> = univDAO.getScheduleByDay(group)
    fun getScheduleByDate(date: String): LiveData<List<Schedule>> = univDAO.getScheduleByDate(date)

    fun addSchedule(schedule: Schedule) {
        executor.execute {
            univDAO.addSchedule(schedule)
        }
    }

    fun addStudent(student: Student) {
        executor.execute {
            univDAO.addStudent(student)
        }
    }

    fun addTeacher(teacher: Teacher) {
        executor.execute {
            univDAO.addTeacher(teacher)
        }
    }
}