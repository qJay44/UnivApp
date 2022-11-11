package edu.muiv.univapp.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleDateLiveData = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()

    val scheduleListLiveData = univRepository.getSchedule()

    val studentScheduleListLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleDateLiveData) { scheduleGroup ->
            univRepository.getScheduleForStudent(scheduleGroup)
        }

    val teacherScheduleLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForTeacher(teacherID)
        }

    fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleDateLiveData.value = scheduleGroup
    }

    fun loadScheduleForTeacher(teacherID: UUID) {
        scheduleForTeacher.value = teacherID
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}