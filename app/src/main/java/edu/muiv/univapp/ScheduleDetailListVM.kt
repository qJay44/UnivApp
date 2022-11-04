package edu.muiv.univapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class ScheduleDetailListVM : ViewModel() {

    private val scheduleRepository = ScheduleRepository.get()
    private val scheduleDateLiveData = MutableLiveData<String>()

    var scheduleLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleDateLiveData) { scheduleDate ->
            scheduleRepository.getScheduleByDate(scheduleDate)
        }

    fun loadSchedule(scheduleDate: String) {
        scheduleDateLiveData.value = scheduleDate
    }
}