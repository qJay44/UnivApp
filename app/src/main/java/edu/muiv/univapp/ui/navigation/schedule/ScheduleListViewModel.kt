package edu.muiv.univapp.ui.navigation.schedule

import android.util.Log
import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.TwoStringListsDifference
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private val user     by lazy { UserDataHolder.get().user }
    private val calendar by lazy { Calendar.getInstance() }
    private val univAPI  by lazy { CoreDatabaseFetcher.get() }
    private val listDiff by lazy { TwoStringListsDifference() }
    private val univRepository = UnivRepository.get()

    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)
    private val days: Array<String> = Array(7) { it.toString() }

    // LiveData variables //

    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()
    private val _dayFromTo = MutableLiveData<String>()
    private val _scheduleFetched = MutableLiveData<Map<StatusCode, List<ScheduleWithSubjectAndTeacher>?>>()
    private var needNewWeekFetch = false

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

    val fetchedSchedule: LiveData<Map<StatusCode, List<ScheduleWithSubjectAndTeacher>?>>
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
        needNewWeekFetch = true

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

    private fun fetchSchedule() {
        // Only if online and didn't fetch yet or new week
        if (UserDataHolder.isInternetAvailable && (_scheduleFetched.value == null || needNewWeekFetch)) {
            needNewWeekFetch = false
            if (isTeacher) {
                univAPI.fetchSchedule(
                    teacherId = user.id,
                    dateStart = days.first(),
                    dateEnd = days.last()
                ) { response ->
                    _scheduleFetched.value = response
                }
            } else {
                univAPI.fetchSchedule(
                    user.groupName!!,
                    dateStart = days.first(),
                    dateEnd = days.last()
                ) { response ->
                    _scheduleFetched.value = response
                }
            }
        }
    }

    fun createScheduleIdList(
        scheduleList: List<ScheduleWithSubjectAndTeacher>,
        type: FetchedListType,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            when (type) {
                // The list from API call
                FetchedListType.NEW -> {
                    listDiff.newList = scheduleList.map { it.id }

                    val diffLists = listDiff.compareLists()
                    val deleteList = diffLists["delete"]
                    val upserteList = scheduleList.filter {
                        it.id in diffLists["upsert"]!!
                    }

                    univRepository.deleteAndUpsertSchedule(deleteList!!, upserteList)
                }
                // The list from the app database
                FetchedListType.OLD -> {
                    listDiff.oldList = scheduleList.map { it.id }
                    fetchSchedule()
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

            return ScheduleWeekDay.MONDAY.dayName
        }

        return ScheduleWeekDay.getDayNameByIndex(dayIndex)
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
}
