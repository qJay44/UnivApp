package edu.muiv.univapp

import androidx.lifecycle.ViewModel

class ScheduleListViewModel : ViewModel() {

    private val scheduleRepository = ScheduleRepository.get()

    val scheduleListLiveData = scheduleRepository.getSchedule()
    val scheduleByDayListLiveData = scheduleRepository.getScheduleByDay()

    fun addSchedule(schedule: Schedule) = scheduleRepository.addSchedule(schedule)
}