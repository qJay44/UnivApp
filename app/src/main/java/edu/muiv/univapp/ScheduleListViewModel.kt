package edu.muiv.univapp

import androidx.lifecycle.ViewModel
import java.util.Date

class ScheduleListViewModel : ViewModel() {

    private val scheduleRepository = ScheduleRepository.get()
    val scheduleListLiveData = scheduleRepository.getSchedule()
    val scheduleByDayListLiveData = scheduleRepository.getScheduleByDay()

    fun getScheduleByDate(date: Date) = scheduleRepository.getScheduleByDate(date)

    fun addSchedule(schedule: Schedule) = scheduleRepository.addSchedule(schedule)
}