package edu.muiv.univapp.ui.navigation.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.UserDataHolder
import java.text.SimpleDateFormat
import java.util.*

class NotificationListViewModel : ViewModel() {
    private val user by lazy { UserDataHolder.get().user }
    private val univAPI by lazy { CoreDatabaseFetcher.get() }
    private val univRepository = UnivRepository.get()
    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)
    private val _notificationsForStudent = MutableLiveData<List<String>>()
    private val _notificationsForTeacher = MutableLiveData<List<String>>()
    private val _notificationsFetched = MutableLiveData<Map<Int, List<Notification>?>>()

    val isTeacher: Boolean
        get() = user.groupName == null

    val fetchedNotifications: LiveData<Map<Int, List<Notification>?>>
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

    fun fetchNotifications() {
        if (UserDataHolder.isServerOnline) {
            univAPI.fetchNotifications(user.groupName!!) { response ->
                _notificationsFetched.value = response
            }
        }
    }

    fun upsertNotifications(notifications: List<Notification>) {
        univRepository.upsertNotifications(notifications)
    }
}
