package edu.muiv.univapp.schedule

import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository

class ScheduleListViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()

    val scheduleListLiveData = univRepository.getSchedule()
    val scheduleByDayListLiveData = univRepository.getScheduleByDay()

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}