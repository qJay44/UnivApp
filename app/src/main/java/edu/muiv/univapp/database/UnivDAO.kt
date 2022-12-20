package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.ui.login.LoginResult
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.SubjectAndTeacher
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
    fun getScheduleById(id: UUID): LiveData<Schedule>

    @Query("SELECT * FROM schedule WHERE studentGroup=:group AND date IN (:days)")
    fun getScheduleForStudent(group: String, days: Array<String>): LiveData<List<Schedule>>

    @Query(
        "SELECT schedule.* FROM teacher " +
        "INNER JOIN schedule ON teacher.id=schedule.teacherID " +
        "WHERE teacher.id=:teacherID AND date IN (:days)"
    )
    fun getScheduleForTeacher(teacherID: UUID, days: Array<String>): LiveData<List<Schedule>>

    @Query("SELECT * FROM ScheduleAttendance WHERE scheduleID=:scheduleID AND studentID=:studentID")
    fun getScheduleAttendance(scheduleID: UUID, studentID: UUID): LiveData<ScheduleAttendance?>

    @Query(
        "SELECT student.* FROM ScheduleAttendance " +
        "INNER JOIN student ON ScheduleAttendance.studentID=student.id " +
        "WHERE scheduleID=:scheduleID AND willAttend=1"
    )
    fun getWillAttendStudents(scheduleID: UUID): LiveData<List<Student>>

    @Query("SELECT * FROM ScheduleUserNotes WHERE scheduleID=:scheduleID AND studentID=:studentID")
    fun getScheduleUserNotes(scheduleID: UUID, studentID: UUID): LiveData<ScheduleUserNotes?>

    @Upsert(entity = ScheduleAttendance::class)
    fun upsertScheduleAttendance(scheduleAttendance: ScheduleAttendance)

    @Upsert(entity = ScheduleUserNotes::class)
    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes)

    @Query("SELECT * FROM Notification WHERE date IN (:days)")
    fun getNotifications(days: List<String>): LiveData<List<Notification>>

    @Query(
        "SELECT subjectName, examType, name, surname, patronymic FROM Subject " +
        "INNER JOIN Teacher ON subject.teacherID=teacher.id " +
        "WHERE groupName=:groupName")
    fun getSubjectsAndTeachersByGroupName(groupName: String): LiveData<List<SubjectAndTeacher>>

    @Query("SELECT * FROM ProfileAttendance WHERE userID=:userID")
    fun getProfileAttendance(userID: UUID): LiveData<List<ProfileAttendance>>

    @Insert
    fun addSchedule(schedule: Schedule)

    @Insert
    fun addStudent(student: Student)

    @Insert
    fun addTeacher(teacher: Teacher)

    @Insert
    fun addNotification(notification: Notification)

    @Insert
    fun addProfileAttendance(profileAttendance: ProfileAttendance)

    @Insert
    fun addSubject(subject: Subject)
}
