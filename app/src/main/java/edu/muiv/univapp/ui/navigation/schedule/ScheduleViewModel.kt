package edu.muiv.univapp.ui.navigation.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.utils.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleViewModel : ViewModel() {

    companion object {
        /** [MIN_TIME_DIFFERENCE] in minutes */
        private const val MIN_TIME_DIFFERENCE = 15
    }

    private val univRepository = UnivRepository.get()
    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)

    // LiveData variables //

    private val scheduleDateLiveData = MutableLiveData<UUID>()
    private val teacherIdLiveData = MutableLiveData<Array<UUID>>()
    private val scheduleToStudentLiveData = MutableLiveData<Map<UUID, UUID>>()
    private val scheduleIdLiveData = MutableLiveData<UUID>()
    private val subjectLiveData = MutableLiveData<UUID>()

    ////////////////////////

    var scheduleAttendance: ScheduleAttendance? = null
    var scheduleUserNotes: ScheduleUserNotes? = null

    val isTeacher: Boolean by lazy { getUserType() }

    val isAllowedToCheckAttendance: String
        get() = run {
            val calendarToday = Calendar.getInstance()
            val calendarSchedule = Calendar.getInstance().apply {
                val hourAndMinute = schedule!!.timeStart.split(":")

                time = originalDateFormat.parse(schedule!!.date)!!
                set(Calendar.HOUR_OF_DAY, hourAndMinute[0].toInt())
                set(Calendar.MINUTE, hourAndMinute[1].toInt())
                set(Calendar.SECOND, 0)
            }

            val currentDateMillis = calendarToday.time.time
            val scheduleDateMillis = calendarSchedule.time.time

            // From minutes to milliseconds
            val minTimeDifference = MIN_TIME_DIFFERENCE * 60 * 1000

            // Allow student to use the button only when online and 15 minutes before subject start
            if (!UserDataHolder.isOnline) {
                "Offline"
            } else if (currentDateMillis > scheduleDateMillis) {
                "Late"
            } else if (scheduleDateMillis - currentDateMillis <= minTimeDifference) {
                "Allowed"
            } else {
                "Early"
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
    }

    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes) {
        if (isTeacher) univRepository.updateScheduleNotes(schedule!!)
        else univRepository.upsertScheduleUserNotes(scheduleUserNotes)
    }
}
