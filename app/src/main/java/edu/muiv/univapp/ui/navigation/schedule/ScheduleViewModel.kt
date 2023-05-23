package edu.muiv.univapp.ui.navigation.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.ui.navigation.schedule.utils.AttendanceStatus
import edu.muiv.univapp.utils.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel : ViewModel() {

    companion object {
        /** [MIN_TIME_DIFFERENCE] in minutes */
        private const val MIN_TIME_DIFFERENCE = 15
    }
    private val univApi by lazy { CoreDatabaseFetcher.get() }
    private val univRepository = UnivRepository.get()
    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)

    // LiveData variables //

    private val scheduleDateLiveData = MutableLiveData<UUID>()
    private val teacherIdLiveData = MutableLiveData<Array<UUID>>()
    private val scheduleToStudentLiveData = MutableLiveData<Map<UUID, UUID>>()
    private val scheduleIdLiveData = MutableLiveData<UUID>()
    private val subjectLiveData = MutableLiveData<UUID>()

    private val _fetchedScheduleAttendanceForStudent = MutableLiveData<Map<StatusCode, ScheduleAttendance?>>()
    private val _upsertAttendanceStatus = MutableLiveData<StatusCode>()
    private val _fetchForTeacherStatus = MutableLiveData<String>()
    private val _updateScheduleStatus = MutableLiveData<StatusCode>()

    ////////////////////////

    var scheduleAttendance: ScheduleAttendance? = null
    var scheduleUserNotes: ScheduleUserNotes? = null

    val isTeacher: Boolean by lazy { getUserType() }

    val isAllowedToCheckAttendance: String?
        get() {
            val calendarToday = Calendar.getInstance()
            val calendarSchedule = Calendar.getInstance().apply {
                val hourAndMinute = schedule!!.timeStart.split(":")

                time = originalDateFormat.parse(schedule!!.date)!!
                set(Calendar.HOUR_OF_DAY, hourAndMinute[0].toInt())
                set(Calendar.MINUTE, hourAndMinute[1].toInt())
                set(Calendar.SECOND, 0)
            }

            val currentDateMillis = calendarToday.timeInMillis
            val scheduleDateMillis = calendarSchedule.timeInMillis
            val minTimeDifference = MIN_TIME_DIFFERENCE * 60 * 1000 // milliseconds

            return when {
                !UserDataHolder.isServerOnline -> AttendanceStatus.OFFLINE.status
                currentDateMillis > scheduleDateMillis -> AttendanceStatus.LATE.status
                scheduleDateMillis - currentDateMillis <= minTimeDifference -> AttendanceStatus.ALLOWED.status
                else -> AttendanceStatus.EARLY.status
            }
        }

    var scheduleID: UUID? = null
        set(value) {
            field = value!!
            loadSchedule(value)
            loadAttendance(value, UserDataHolder.get().user.id)
        }

    var schedule: Schedule? = null
        set(value) {
            field = value!!
            loadTeacher(value.teacherID)
            loadSubject(value.subjectID)
        }

    // Public LiveData //

    val scheduleLiveData: LiveData<Schedule> =
        Transformations.switchMap(scheduleDateLiveData) { id ->
            univRepository.getScheduleById(id)
        }

    val teacherLiveData: LiveData<Array<Teacher>> =
        Transformations.switchMap(teacherIdLiveData) { IDs ->
            univRepository.getTeachersByIDs(IDs)
        }

    val scheduleAttendanceLiveData: LiveData<ScheduleAttendance?> =
        Transformations.switchMap(scheduleToStudentLiveData) { MapId ->
            univRepository.getScheduleAttendance(
                MapId.keys.elementAt(0), MapId.values.elementAt(0)
            )
        }

    val scheduleUserNotesLiveData: LiveData<ScheduleUserNotes?> =
        Transformations.switchMap(scheduleToStudentLiveData) { MapId ->
            univRepository.getScheduleUserNotes(
                MapId.keys.elementAt(0), MapId.values.elementAt(0)
            )
        }

    val studentsWillAttend: LiveData<List<Student>> =
        Transformations.switchMap(scheduleIdLiveData) { id ->
            univRepository.getWillAttendStudents(id)
        }

    val subject: LiveData<Subject> =
        Transformations.switchMap(subjectLiveData) { id ->
            univRepository.getSubjectById(id)
        }

    val fetchedScheduleAttendanceForStudent: LiveData<Map<StatusCode, ScheduleAttendance?>>
        get() = _fetchedScheduleAttendanceForStudent

    val upsertAttendanceStatus: LiveData<StatusCode>
        get() = _upsertAttendanceStatus

    val fetchForTeacherStatus: LiveData<String>
        get() = _fetchForTeacherStatus

    val updateScheduleStatus: LiveData<StatusCode>
        get() = _updateScheduleStatus

    /////////////////////

    private fun getUserType(): Boolean = UserDataHolder.get().user.groupName == null

    private fun loadSchedule(scheduleID: UUID) {
        scheduleDateLiveData.value = scheduleID
    }

    private fun loadAttendance(scheduleID: UUID, userID: UUID) {
        val idMap = mapOf(scheduleID to userID)
        if (isTeacher)
            scheduleIdLiveData.value = scheduleID
        else
            scheduleToStudentLiveData.value = idMap

        if (UserDataHolder.isServerOnline) {
            if (isTeacher) {
                univApi.fetchScheduleAttendanceForTeacher(scheduleID.toString()) { response ->
                    val statusCode = response.keys.first()
                    val scheduleAttendance = response.values.first()

                    if (statusCode == StatusCode.OK) {
                        univRepository.upsertScheduleAttendance(scheduleAttendance!!)
                        _fetchForTeacherStatus.value = "Upserting ${scheduleAttendance.size} for teacher"
                    }
                    else {
                        _fetchForTeacherStatus.value = "Invalid code for upserting ($statusCode)"
                    }
                }
            } else {
                univApi.fetchScheduleAttendanceForStudent(scheduleID.toString(), userID.toString()) { response ->
                    _fetchedScheduleAttendanceForStudent.value = response
                }
            }
        }
    }

    private fun loadTeacher(id: UUID) {
        val idArr = Array(1) { id }
        teacherIdLiveData.value = idArr
    }

    private fun loadSubject(id: UUID) {
        subjectLiveData.value = id
    }

    fun upsertAttendance(scheduleAttendance: ScheduleAttendance) {
        univRepository.upsertScheduleAttendance(scheduleAttendance)
        univApi.updateScheduleAttendance(scheduleAttendance) { response ->
            _upsertAttendanceStatus.value = response
        }
    }

    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes) {
        if (isTeacher) univRepository.updateScheduleNotes(schedule!!)
        else univRepository.upsertScheduleUserNotes(scheduleUserNotes)
    }

    fun updateScheduleInCoreDatabase() {
        univApi.updateSchedule(schedule!!) { response ->
            _updateScheduleStatus.value = response
        }
    }
}
