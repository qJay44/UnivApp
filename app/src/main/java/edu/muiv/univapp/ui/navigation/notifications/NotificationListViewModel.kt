package edu.muiv.univapp.ui.navigation.notifications

import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.TwoStringListsDifference
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationListViewModel : ViewModel() {
    private val user     by lazy { UserDataHolder.get().user }
    private val univAPI  by lazy { CoreDatabaseFetcher.get() }
    private val listDiff by lazy { TwoStringListsDifference() }
    private val univRepository = UnivRepository.get()
    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)

    private val _notificationsForStudent = MutableLiveData<List<String>>()
    private val _notificationsForTeacher = MutableLiveData<List<String>>()
    private val _notificationsFetched = MutableLiveData<Map<StatusCode, List<Notification>?>>()

    val isTeacher: Boolean
        get() = user.groupName == null

    val fetchedNotifications: LiveData<Map<StatusCode, List<Notification>?>>
        get() = _notificationsFetched

    val notificationsForStudent: LiveData<List<Notification>> =
        Transformations.switchMap(_notificationsForStudent) { notificationsDays ->
            univRepository.getNotificationsForStudent(notificationsDays, user.groupName!!)
        }

    val notificationsForTeacher: LiveData<List<Notification>> =
        Transformations.switchMap(_notificationsForTeacher) { notificationsDays ->
            univRepository.getNotificationsForTeacher(notificationsDays)
        }

    private fun getMonthDays(calendar: Calendar): List<String> {
        val currentMonth = calendar.get(Calendar.MONTH)
        val days = mutableListOf<String>()

        while (currentMonth == calendar.get(Calendar.MONTH)) {
            days += originalDateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days.toList()
    }

    private fun fetchNotifications() {
        // Only if online and didn't fetch yet
        if (UserDataHolder.isServerOnline && _notificationsFetched.value == null) {
            if (!isTeacher) {
                univAPI.fetchNotifications(user.groupName!!) { response ->
                    _notificationsFetched.value = response
                }
            }
        }
    }

    fun createNotificationsIdList(notifications: List<Notification>, type: FetchedListType) {
        viewModelScope.launch(Dispatchers.Default) {
            when (type) {
                // The list from API call
                FetchedListType.NEW -> {
                    listDiff.newList = notifications.map { it.id }

                    val diffLists = listDiff.compareLists()
                    val deleteList = diffLists["delete"]
                    val upserteList = notifications.filter {
                        it.id in diffLists["upsert"]!!
                    }
                    univRepository.deleteAndUpsertNotifications(deleteList!!, upserteList)
                }
                // The list from the app database
                FetchedListType.OLD -> {
                    listDiff.oldList = notifications.map { it.id }
                    fetchNotifications()
                }
            }
        }
    }

    fun getSimpleDate(dateString: String): String {
        val date = originalDateFormat.parse(dateString)!!
        val formatOut = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru"))

        return formatOut.format(date)
    }

    fun loadNotifications() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -1)
        }

        // Month change (next month) every function call
        val previousMonthDays = getMonthDays(calendar)
        val currentMonthDays = getMonthDays(calendar)

        if (isTeacher)
            _notificationsForTeacher.value = previousMonthDays + currentMonthDays
        else
            _notificationsForStudent.value = previousMonthDays + currentMonthDays
    }
}
