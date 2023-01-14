package edu.muiv.univapp.ui.navigation.schedule

import android.util.Log
import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private val user by lazy { UserDataHolder.get().user }
    private val calendar by lazy { Calendar.getInstance() }
    private val univAPI by lazy { CoreDatabaseFetcher.get() }
    private val univRepository = UnivRepository.get()

    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)
    private val days: Array<String> = Array(7) { it.toString() }

    // LiveData variables //

    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()
    private val _dayFromTo = MutableLiveData<String>()
    private val _scheduleFetched = MutableLiveData<Map<Int, List<ScheduleWithSubjectAndTeacher>?>>()

    private var scheduleIdListNew: MutableList<UUID>? = null
    private var scheduleIdListOld: MutableList<UUID>? = null
    private var scheduleIdListToDelete: MutableList<UUID>? = null

    ////////////////////////

    // Simple properties //

    val isTeacher: Boolean
        get() = user.groupName == null

    //////////////////////////

    // LiveData repository observables //

    val teacherSchedulesLiveData: LiveData<List<ScheduleWithSubjectAndTeacher>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForWeek(teacherID, days.toList())
        }

    val studentSchedule: LiveData<List<ScheduleWithSubjectAndTeacher>> =
        Transformations.switchMap(scheduleForStudent) { scheduleGroup ->
            univRepository.getScheduleForWeek(scheduleGroup, days.toList())
        }

    val dayFromTo: LiveData<String>
        get() = _dayFromTo

    val fetchedSchedule: LiveData<Map<Int, List<ScheduleWithSubjectAndTeacher>?>>
        get() = _scheduleFetched

    /////////////////////////////////////

    // LiveData value setters //

    private fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleForStudent.value = scheduleGroup
    }

    private fun loadScheduleForTeacher(teacherID: UUID) {
        scheduleForTeacher.value = teacherID
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
        for (i in days.indices) {
            days[i] = originalDateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Subtract extra added day
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        val firstDay = getSimpleDate(days[0])
        val lastDay = getSimpleDate(days.last())

        _dayFromTo.value = "$firstDay - $lastDay"
        loadSchedule()
    }

    private fun deleteScheduleById(idList: List<UUID>) {
        univRepository.deleteScheduleById(idList)
    }

    private fun updateDatabase() {
        scheduleIdListToDelete = mutableListOf()
        for (oldId in scheduleIdListOld!!) {
            if (oldId !in scheduleIdListNew!!) {
                scheduleIdListToDelete!!.add(oldId)
            }
        }
        deleteScheduleById(scheduleIdListToDelete!!)

        scheduleIdListNew = null
        scheduleIdListOld = null
        scheduleIdListToDelete = null
    }

    fun createScheduleIdList(scheduleList: List<ScheduleWithSubjectAndTeacher>, type: Int) {
        viewModelScope.launch {
            when (type) {
                // The list from API call
                ScheduleListTypes.NEW.type -> {
                    scheduleIdListNew = mutableListOf()
                    scheduleList.forEach { scheduleIdListNew!!.add(it.id) }
                    if (!scheduleIdListOld.isNullOrEmpty()) updateDatabase()
                }
                // The list from the app database
                ScheduleListTypes.OLD.type -> {
                    scheduleIdListOld = mutableListOf()
                    scheduleList.forEach { scheduleIdListOld!!.add(it.id) }
                    if (!scheduleIdListNew.isNullOrEmpty()) updateDatabase()
                }
            }
        }
    }

    fun getWeekDayNameByDate(dateString: String): String {
        val dayIndex = days.indexOf(dateString)

        /** FIXME: Resolve case when got unexpected [dateString] */
        if (dayIndex == -1) {
            Log.e("ScheduleListFragmentVM", "dateString: $dateString")
            Log.e("ScheduleListFragmentVM", "Days: ${days.toList()}")
        }

        return ScheduleWeekDays.getDayNameByIndex(dayIndex)
    }

    fun getSimpleDate(dateString: String): String {
        val date = originalDateFormat.parse(dateString)!!
        val formatOut = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru"))

        return formatOut.format(date)
    }

    fun loadCalendar() {
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        loadDays()
    }

    fun loadPreviousWeek() {
        // Set current day to Monday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Subtract one week
        calendar.add(Calendar.WEEK_OF_MONTH, -1)
        loadDays()
    }

    fun loadNextWeek() {
        // Set current day to Monday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Add one week
        calendar.add(Calendar.WEEK_OF_MONTH, 1)
        loadDays()
    }

    fun fetchSchedule() {
        if (UserDataHolder.isOnline) {
            if (isTeacher) {
                univAPI.fetchSchedule(teacherId = user.id) { response ->
                    _scheduleFetched.value = response
                }
            } else {
                univAPI.fetchSchedule(user.groupName!!) { response ->
                    _scheduleFetched.value = response
                }
            }
        }
    }

    fun upsertSchedule(scheduleList: List<ScheduleWithSubjectAndTeacher>) {
        univRepository.upsertSchedule(scheduleList)
    }
}
