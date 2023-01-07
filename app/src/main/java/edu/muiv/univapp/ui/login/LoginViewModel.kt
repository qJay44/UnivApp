package edu.muiv.univapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.api.ExternalDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher

class LoginViewModel : ViewModel() {

    private val login = Login()
    private val loginTW = LoginTextWatcher(login)
    private val univRepository by lazy { UnivRepository.get() }
    private val univAPI by lazy { ExternalDatabaseFetcher.get() }

    private val _responseCode = MutableLiveData<Int>()

    val responseCode: LiveData<Int>
        get() = _responseCode

    val usernameTW get() = loginTW.usernameTW
    val passwordTW get() = loginTW.passwordTW

    fun inputValidation(): String? {
        val inputErrorText =
            when ("") {
                login.username -> "Логин не может быть пустым"
                login.password -> "Пароль не может быть пустым"
                else -> null
            }

        return inputErrorText
    }

    fun loadUser(isTeacher: Boolean) {
        login.isTeacher = isTeacher

        univAPI.fetchUser(login) { statusCode ->
            _responseCode.value = statusCode
        }
    }

    fun addStudent(student: Student) {
        univRepository.addStudent(student)
    }

    fun addTeacher(teacher: Teacher) {
        univRepository.addTeacher(teacher)
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }

    fun addNotification(notification: Notification) {
        univRepository.addNotification(notification)
    }

    fun addProfileAttendance(profileAttendance: ProfileAttendance) {
        univRepository.addProfileAttendance(profileAttendance)
    }

    fun addSubject(subject: Subject) {
        univRepository.addSubject(subject)
    }
}
