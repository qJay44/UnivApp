package edu.muiv.univapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder
import java.util.UUID

class ScheduleDetailListVM : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleDateLiveData = MutableLiveData<UUID>()
    private val teacherIdLiveData = MutableLiveData<Array<UUID>>()
    private val scheduleMapLiveData = MutableLiveData<Map<UUID, UUID>>()
    var scheduleAttendance: ScheduleAttendance? = null

    var scheduleID: UUID? = null
        set(value) {
            field = value
            loadSchedule(value!!)
            loadAttendance(value, UserDataHolder.get().user.id)
        }

    var scheduleLiveData: LiveData<Schedule> =
        Transformations.switchMap(scheduleDateLiveData) { id ->
            univRepository.getScheduleById(id)
        }

    var teacherLiveData: LiveData<Array<Teacher>> =
        Transformations.switchMap(teacherIdLiveData) { IDs ->
            univRepository.getTeachersByIDs(IDs)
        }

    var scheduleAttendanceLiveData: LiveData<ScheduleAttendance?> =
        Transformations.switchMap(scheduleMapLiveData) { MapId ->
            univRepository.getScheduleAttendance(
                MapId.keys.elementAt(0), MapId.values.elementAt(0)
            )
        }

    private fun loadSchedule(scheduleID: UUID) {
        scheduleDateLiveData.value = scheduleID
    }

    private fun loadAttendance(scheduleID: UUID, studentID: UUID) {
        val idMap = mapOf(scheduleID to studentID)
        scheduleMapLiveData.value = idMap
    }

    fun loadTeacher(id: UUID) {
        val idArr = Array(1) { id }
        teacherIdLiveData.value = idArr
    }

    fun upsertAttendance(scheduleAttendance: ScheduleAttendance) {
        univRepository.upsertScheduleAttendance(scheduleAttendance)
    }
}