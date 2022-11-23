package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.muiv.univapp.ui.login.LoginResult
import edu.muiv.univapp.ui.schedule.Schedule
import edu.muiv.univapp.user.*
import java.util.UUID

@Dao
interface UnivDAO {

    @Query("SELECT * FROM student WHERE login=:username AND password=:password")
    fun getStudent(username: String, password: String): LiveData<LoginResult>

    @Query("SELECT * FROM teacher WHERE login=:username AND password=:password")
    fun getTeacher(username: String, password: String): LiveData<LoginResult>

    @Query("SELECT * FROM teacher WHERE id IN (:IDs)")
    fun getTeachersByIDs(IDs: Array<UUID>): LiveData<Array<Teacher>>

    @Query("SELECT * FROM schedule WHERE id=:id")
    fun getScheduleById(id: String): LiveData<Schedule>

    @Query("SELECT * FROM schedule WHERE studentGroup=:group AND date IN (:days)")
    fun getScheduleForStudent(group: String, days: Array<String>): LiveData<List<Schedule>>

    @Query(
        "SELECT schedule.* FROM teacher " +
        "INNER JOIN schedule ON teacher.id=schedule.teacherID " +
        "WHERE teacher.id=:teacherID AND date IN (:days)"
    )
    fun getScheduleForTeacher(teacherID: UUID, days: Array<String>): LiveData<List<Schedule>>

    @Insert
    fun addSchedule(schedule: Schedule)

    @Insert
    fun addStudent(student: Student)

    @Insert
    fun addTeacher(teacher: Teacher)
}