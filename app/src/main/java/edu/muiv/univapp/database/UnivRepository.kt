package edu.muiv.univapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import edu.muiv.univapp.api.ScheduleAttendanceForTeacherResponse
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

    fun getTeachersByIDs(IDs: Array<UUID>): LiveData<Array<Teacher>> = univDAO.getTeachersByIDs(IDs)
    fun getScheduleById(id: UUID): LiveData<Schedule> = univDAO.getScheduleById(id)
    fun getScheduleForWeek(groupName: String, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>> = univDAO.getScheduleForWeek(groupName, days)
    fun getScheduleForWeek(id: UUID, days: List<String>): LiveData<List<ScheduleWithSubjectAndTeacher>> = univDAO.getScheduleForWeek(id, days)
    fun getSubjectById(id: UUID): LiveData<Subject> = univDAO.getSubjectById(id)
    fun getScheduleAttendance(scheduleID: UUID, studentID: UUID): LiveData<ScheduleAttendance?> = univDAO.getScheduleAttendance(scheduleID, studentID)
    fun getWillAttendStudents(scheduleID: UUID): LiveData<List<Student>> = univDAO.getWillAttendStudents(scheduleID)
    fun getScheduleUserNotes(scheduleID: UUID, studentID: UUID): LiveData<ScheduleUserNotes?> = univDAO.getScheduleUserNotes(scheduleID, studentID)
    fun getNotificationsForStudent(days: List<String>, groupName: String): LiveData<List<Notification>> = univDAO.getNotificationsForStudent(days, groupName)
    fun getNotificationsForTeacher(days: List<String>): LiveData<List<Notification>> = univDAO.getNotificationsForTeacher(days)
    fun getSubjectsAndTeachers(groupName: String): LiveData<List<SubjectAndTeacher>> = univDAO.getSubjectsAndTeachersByGroupName(groupName)
    fun getProfileAttendance(userID: UUID): LiveData<List<ProfileAttendance>> = univDAO.getProfileAttendance(userID)

    fun upsertScheduleAttendance(scheduleAttendance: ScheduleAttendance) {
        executor.execute {
            univDAO.upsertScheduleAttendance(scheduleAttendance)
        }
    }

    fun upsertScheduleAttendance(scheduleAttendanceList: List<ScheduleAttendanceForTeacherResponse>) {
        executor.execute {
            for (sa in scheduleAttendanceList) {
                with(sa) {
                    val scheduleAttendance = ScheduleAttendance(
                        scheduleAttendanceId,
                        scheduleID,
                        studentID,
                        willAttend
                    )
                    univDAO.upsertScheduleAttendance(scheduleAttendance)
                }
            }
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

    fun deleteAndUpsertSchedule(deleteList: List<String>, upsertList: List<ScheduleWithSubjectAndTeacher>) {
        executor.execute {
            val scheduleList = mutableListOf<Schedule>()
            for (scheduleWithSubjectAndTeacher in upsertList) {
                with(scheduleWithSubjectAndTeacher) {
                    val tch = Teacher(
                        UUID.fromString(teacherID),
                        teacherName,
                        teacherSurname,
                        teacherPatronymic,
                    )

                    univDAO.upsertTeacher(tch)

                    val sch = Schedule(
                        UUID.fromString(id),
                        date,
                        timeStart,
                        timeEnd,
                        roomNum,
                        type,
                        teacherNotes,
                        UUID.fromString(subjectID),
                        UUID.fromString(teacherID)
                    )

                    scheduleList += sch
                }
            }
            univDAO.deleteAndUpsertSchedule(deleteList, scheduleList)
        }
    }

    fun deleteAndUpsertNotifications(deleteList: List<String>, upsertList: List<Notification>) {
        executor.execute {
            univDAO.deleteAndUpsertNotifications(deleteList, upsertList)
        }
    }

    fun deleteAndUpsertSubjects(deleteList: List<String>, upsertList: List<SubjectAndTeacher>) {
        executor.execute {
            val subjects = mutableListOf<Subject>()
            for (subjectAndTeacher in upsertList) {
                with(subjectAndTeacher) {
                    val teacher = Teacher(
                        UUID.fromString(teacherID),
                        teacherName,
                        teacherSurname,
                        teacherPatronymic,
                    )

                    univDAO.upsertTeacher(teacher)

                    val subject = Subject(
                        subjectID,
                        subjectName,
                        subjectGroupName,
                        teacherID,
                        subjectExamType
                    )

                    subjects += subject
                }
            }

            univDAO.deleteAndUpsertSubjects(deleteList, subjects)
        }
    }

    fun deleteAndUpsertProfileAttendance(deleteList: List<String>, profileAttendances: List<ProfileAttendance>) {
        executor.execute {
            univDAO.deleteAndUpsertProfileAttendance(deleteList, profileAttendances)
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
