package edu.muiv.univapp.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()

    val scheduleListLiveData = univRepository.getSchedule()

    val studentSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForStudent) { scheduleGroup ->
            univRepository.getScheduleForStudent(scheduleGroup)
        }

    val teacherSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForTeacher(teacherID)
        }

    fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleForStudent.value = scheduleGroup
    }

    fun loadScheduleForTeacher(teacherID: UUID) {
        scheduleForTeacher.value = teacherID
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}