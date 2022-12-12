package edu.muiv.univapp.ui.navigation.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleUserNotes
import edu.muiv.univapp.user.Student
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder
import java.util.UUID

class ScheduleViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleDateLiveData = MutableLiveData<UUID>()
    private val teacherIdLiveData = MutableLiveData<Array<UUID>>()
    private val scheduleToStudentLiveData = MutableLiveData<Map<UUID, UUID>>()
    private val scheduleIdLiveData = MutableLiveData<UUID>()
    var scheduleAttendance: ScheduleAttendance? = null
    var scheduleUserNotes: ScheduleUserNotes? = null

    val isTeacher: Boolean by lazy { getUserType() }

    var scheduleID: UUID? = null
        set(value) {
            field = value
            loadSchedule(value!!)
            loadAttendance(value, UserDataHolder.get().user.id)
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

    fun loadTeacher(id: UUID) {
        val idArr = Array(1) { id }
        teacherIdLiveData.value = idArr
    }

    fun upsertAttendance(scheduleAttendance: ScheduleAttendance) {
        univRepository.upsertScheduleAttendance(scheduleAttendance)
    }

    fun upsertScheduleUserNotes(scheduleUserNotes: ScheduleUserNotes) {
        univRepository.upsertScheduleUserNotes(scheduleUserNotes)
    }
}
