package edu.muiv.univapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.login.LoginResult
import edu.muiv.univapp.user.UserDataHolder
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private lateinit var user: LoginResult
    private val univRepository = UnivRepository.get()
    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()

    // Primitive properties //

    val title: String
        get() = "${user.name} ${user.surname} ${user.groupName ?: ""}"

    val isTeacher: Boolean
        get() = user.groupName == null

    //////////////////////////

    // LiveData properties //

    val scheduleListLiveData = univRepository.getSchedule()

    val studentSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForStudent) { scheduleGroup ->
            univRepository.getScheduleForStudent(scheduleGroup)
        }

    val teacherSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForTeacher(teacherID)
        }

    /////////////////////////

    // LiveData value setters //

    private fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleForStudent.value = scheduleGroup
    }

    private fun loadScheduleForTeacher(teacherID: UUID) {
        // FIXME: Schedules not grouping
        scheduleForTeacher.value = teacherID
    }

    ////////////////////////////

    fun loadUser() {
        user = UserDataHolder.get().user

        if (user.groupName != null) {
            loadScheduleForStudent(user.groupName!!)
        } else {
            loadScheduleForTeacher(user.id)
        }
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}
