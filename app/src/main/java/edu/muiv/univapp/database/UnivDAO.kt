package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import edu.muiv.univapp.login.LoginResult
import edu.muiv.univapp.schedule.Schedule
import edu.muiv.univapp.user.*

@Dao
interface UnivDAO {

    @Query(
        "SELECT * FROM(" +
        "SELECT * FROM student UNION ALL SELECT *, NULL FROM teacher) " +
        "WHERE login=:login AND password=:password"
    )
    fun getUser(login: String, password: String): LiveData<LoginResult>

    @Transaction
    @Query("SELECT * FROM teacher")
    fun getTeacherWithSchedules(): LiveData<TeacherWithSchedules>

    @Query("SELECT * FROM schedule")
    fun getSchedule(): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE studentGroup=:group GROUP BY date")
    fun getScheduleByDay(group: String): LiveData<List<Schedule>>

    @Query("SELECT * FROM schedule WHERE date=:date")
    fun getScheduleByDate(date: String): LiveData<List<Schedule>>

    @Insert
    fun addSchedule(schedule: Schedule)

    @Insert
    fun addStudent(student: Student)

    @Insert
    fun addTeacher(teacher: Teacher)
}