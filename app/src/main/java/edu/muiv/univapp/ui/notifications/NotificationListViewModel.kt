package edu.muiv.univapp.ui.notifications

import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository

class NotificationListViewModel : ViewModel() {

    private val repository = UnivRepository.get()
    private val _notifications = repository.getNotifications()

    val notifications
        get() = _notifications
}