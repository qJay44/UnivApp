package edu.muiv.univapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.ui.login.Login
import edu.muiv.univapp.ui.login.LoginResult
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.ui.navigation.profile.SubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import java.util.UUID
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

    fun getUser(login: Login): LiveData<LoginResult> {
        return if (login.isTeacher) {
            univDAO.getTeacher(login.username, login.password)
        } else {
            univDAO.getStudent(login.username, login.password)
        }
    }

    fun getTeachersByIDs(IDs: Array<UUID>): LiveData<Array<Teacher>> = univDAO.getTeachersByIDs(IDs)
    fun getScheduleById(id: UUID): LiveData<Schedule> = univDAO.getScheduleById(id)
    fun getScheduleForWeek(groupName: String, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>> = univDAO.getScheduleForWeek(groupName, days)
    fun getScheduleForWeek(id: UUID, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>> = univDAO.getScheduleForWeek(id, days)
    fun getSubjectById(id: UUID): LiveData<Subject> = univDAO.getSubjectById(id)
    fun getScheduleAttendance(scheduleID: UUID, studentID: UUID): LiveData<ScheduleAttendance?> = univDAO.getScheduleAttendance(scheduleID, studentID)
    fun getWillAttendStudents(scheduleID: UUID): LiveData<List<Student>> = univDAO.getWillAttendStudents(scheduleID)
    fun getScheduleUserNotes(scheduleID: UUID, studentID: UUID): LiveData<ScheduleUserNotes?> = univDAO.getScheduleUserNotes(scheduleID, studentID)
    fun getNotifications(days: List<String>): LiveData<List<Notification>> = univDAO.getNotifications(days)
    fun getSubjectsAndTeachers(groupName: String): LiveData<List<SubjectAndTeacher>> = univDAO.getSubjectsAndTeachersByGroupName(groupName)
    fun getProfileAttendance(userID: UUID): LiveData<List<ProfileAttendance>> = univDAO.getProfileAttendance(userID)

    fun upsertScheduleAttendance(scheduleAttendance: ScheduleAttendance) {
        executor.execute {
            univDAO.upsertScheduleAttendance(scheduleAttendance)
        }
    }

    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes) {
        executor.execute {
            univDAO.upsertScheduleUserNotes(scheduleUserNotes)
        }
    }

    fun updateScheduleNotes(schedule: Schedule) {
        executor.execute {
            univDAO.updateScheduleNotes(schedule)
        }
    }

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

    fun addNotification(notification: Notification) {
        executor.execute {
            univDAO.addNotification(notification)
        }
    }

    fun addProfileAttendance(profileAttendance: ProfileAttendance) {
        executor.execute {
            univDAO.addProfileAttendance(profileAttendance)
        }
    }

    fun addSubject(subject: Subject) {
        executor.execute {
            univDAO.addSubject(subject)
        }
    }
}
