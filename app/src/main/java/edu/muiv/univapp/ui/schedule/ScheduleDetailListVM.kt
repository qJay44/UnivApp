package edu.muiv.univapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository

class ScheduleDetailListVM : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val scheduleDateLiveData = MutableLiveData<String>()

    var scheduleLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleDateLiveData) { scheduleDate ->
            univRepository.getScheduleByDate(scheduleDate)
        }

    fun loadSchedule(scheduleDate: String) {
        scheduleDateLiveData.value = scheduleDate
    }
}