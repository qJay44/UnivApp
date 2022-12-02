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
    private lateinit var calendar: Calendar

    private val univRepository = UnivRepository.get()
    private val days: Array<String> = Array(7) { it.toString() }

    // LiveData variables //

    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()
    private val weekTeachers = MutableLiveData<Array<UUID>>()

    ////////////////////////

    // Simple properties //

    val isTeacher: Boolean
        get() = user.groupName == null

    val dayFromTo: String
        get() = "${days[0]} - ${days.last()}"

    val week: Set<String>
        get() = days.toSet()

    //////////////////////////

    // LiveData repository observables //

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

    /////////////////////////////////////

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

    private fun loadSchedule() {
        if (isTeacher) {
            loadScheduleForTeacher(user.id)
        } else {
            loadScheduleForStudent(user.groupName!!)
        }
    }

    private fun loadDays() {
        val format = SimpleDateFormat("dd.MM", Locale.FRANCE)

        for (i in days.indices) {
            days[i] = format.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Subtract extra added day
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        loadSchedule()
    }

    fun loadUser() {
        user = UserDataHolder.get().user
    }

    fun loadCalendar() {
        calendar = Calendar.getInstance(Locale.FRANCE)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        loadDays()
    }

    fun prevWeek() {
        // Set current day to Monday
        calendar.add(Calendar.DAY_OF_MONTH, -6)
        // Subtract one week
        calendar.add(Calendar.WEEK_OF_MONTH, -1)
        loadDays()
    }

    fun nextWeek() {
        // Set current day to Monday
        calendar.add(Calendar.DAY_OF_MONTH, -6)
        // Add one week
        calendar.add(Calendar.WEEK_OF_MONTH, 1)
        loadDays()
    }
}
