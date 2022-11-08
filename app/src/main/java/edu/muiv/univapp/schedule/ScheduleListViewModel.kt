package edu.muiv.univapp.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository

class ScheduleListViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleDateLiveData = MutableLiveData<String>()

    val scheduleListLiveData = univRepository.getSchedule()

    val scheduleByDayListLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleDateLiveData) { scheduleGroup ->
            univRepository.getScheduleByDay(scheduleGroup)
        }

    fun loadSchedule(scheduleGroup: String) {
        scheduleDateLiveData.value = scheduleGroup
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}