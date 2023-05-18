package edu.muiv.univapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.ui.navigation.profile.SubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import java.util.UUID

@Dao
interface UnivDAO {
    // Do not forget: place " " at the end if there is a new line comes after

    @Query("SELECT * FROM teacher WHERE id IN (:IDs)")
    fun getTeachersByIDs(IDs: Array<UUID>): LiveData<Array<Teacher>>

    @Query("SELECT * FROM schedule WHERE id=:id")
    fun getScheduleById(id: UUID): LiveData<Schedule>

    @Query(
        "SELECT " +
                "schedule.id, date, timeStart, timeEnd, roomNum, type, teacherNotes," +
                "subject.id AS subjectID, subjectName, groupName, teacher.id AS teacherID," +
                "name, surname, patronymic " +
        "FROM Schedule " +
        "INNER JOIN Subject ON schedule.subjectID=subject.id " +
        "INNER JOIN Teacher ON subject.teacherID=teacher.id " +
        "WHERE subject.groupName=:groupName AND date IN (:days)"
    )
    fun getScheduleForWeek(groupName: String, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>>

    @Query(
        "SELECT " +
                "schedule.id, teacher.id AS teacherID, date, timeStart, timeEnd, roomNum, type," +
                "teacherNotes, subject.id AS subjectID, subjectName, groupName, name, surname, patronymic " +
        "FROM Schedule " +
        "INNER JOIN Subject ON schedule.subjectID=subject.id " +
        "INNER JOIN Teacher ON subject.teacherID=teacher.id " +
        "WHERE schedule.teacherID=:teacherIdParam AND date IN (:days) GROUP BY date, timeStart"
    )
    fun getScheduleForWeek(teacherIdParam: UUID, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>>

    @Query("SELECT * FROM Subject WHERE id=:id")
    fun getSubjectById(id: UUID): LiveData<Subject>

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

    @Query("SELECT * FROM Notification WHERE date IN (:days) AND studentGroup=:groupName")
    fun getNotificationsForStudent(days: List<String>, groupName: String): LiveData<List<Notification>>

    @Query("SELECT * FROM Notification WHERE date IN (:days)")
    fun getNotificationsForTeacher(days: List<String>): LiveData<List<Notification>>

    @Query(
        "SELECT Subject.* , name, surname, patronymic FROM Subject " +
        "INNER JOIN Teacher ON subject.teacherID=teacher.id " +
        "WHERE groupName=:groupName"
    )
    fun getSubjectsAndTeachersByGroupName(groupName: String): LiveData<List<SubjectAndTeacher>>

    @Query("SELECT * FROM ProfileAttendance WHERE userID=:userID")
    fun getProfileAttendance(userID: UUID): LiveData<List<ProfileAttendance>>

    @Query("DELETE FROM Schedule WHERE id IN (:idList)")
    fun deleteScheduleById(idList: List<String>)

    @Query("DELETE FROM Notification WHERE id IN (:idList)")
    fun deleteNotificationsById(idList: List<String>)

    @Query("DELETE FROM Subject WHERE id IN (:idList)")
    fun deleteSubjectsById(idList: List<String>)

    @Query("DELETE FROM ProfileAttendance WHERE id IN (:idList)")
    fun deleteProfileAttendanceById(idList: List<String>)

    @Update(entity = Schedule::class)
    fun updateScheduleNotes(schedule: Schedule)

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

    @Upsert(entity = ScheduleAttendance::class)
    fun upsertScheduleAttendance(scheduleAttendance: ScheduleAttendance)

    @Upsert(entity = ScheduleUserNotes::class)
    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes)

    @Upsert
    fun upsertNotifications(notifications: Notification)

    @Upsert
    fun upsertSchedule(schedule: Schedule)

    @Upsert
    fun upsertTeacher(teacher: Teacher)

    @Upsert
    fun upsertSubject(subject: Subject)

    @Upsert
    fun upsertProfileAttendance(profileAttendance: ProfileAttendance)
}
