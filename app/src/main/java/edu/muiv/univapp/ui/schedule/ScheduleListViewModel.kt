package edu.muiv.univapp.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.login.LoginResult
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private lateinit var user: LoginResult
    private val univRepository = UnivRepository.get()
    private val weekTeachers = MutableLiveData<Array<UUID>>()
    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()
    var days: Array<String> = Array(7) { it.toString() }

    // Primitive properties //

    val isTeacher: Boolean
        get() = user.groupName == null

    val dayFromTo: String
        get() = "${days[0]} - ${days.last()}"

    //////////////////////////

    // LiveData properties //

    val studentSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForStudent) { scheduleGroup ->
            univRepository.getScheduleForStudent(scheduleGroup, days)
        }

    val teacherSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForTeacher(teacherID, days)
        }

    val weekTeachersLiveData: LiveData<Array<Teacher>> =
        Transformations.switchMap(weekTeachers) { IDs ->
            univRepository.getTeachersByIDs(IDs)
        }
    /////////////////////////

    // LiveData value setters //

    private fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleForStudent.value = scheduleGroup
    }

    private fun loadScheduleForTeacher(teacherID: UUID) {
        scheduleForTeacher.value = teacherID
    }

    fun loadScheduleTeachers(schedules: List<Schedule>) {
        val teacherIDs: Array<UUID> = Array(schedules.size) {
            schedules[it].teacherID
        }
        weekTeachers.value = teacherIDs
    }

    ////////////////////////////

    fun loadCalendar() {
        val format = SimpleDateFormat("dd.MM", Locale.FRANCE)
        val calendar = Calendar.getInstance(Locale.FRANCE)

        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        for (i in 0 until 7) {
            days[i] = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    fun loadUser() {
        user = UserDataHolder.get().user

        if (user.groupName != null) {
            loadScheduleForStudent(user.groupName!!)
        } else {
            loadScheduleForTeacher(user.id)
        }
    }
}
