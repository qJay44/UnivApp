package edu.muiv.univapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.api.ExternalDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher
import edu.muiv.univapp.utils.UserDataHolder

class LoginViewModel : ViewModel() {

    private val login = Login()
    private val loginTW = LoginTextWatcher(login)
    private val univRepository = UnivRepository.get()
    private val univAPI by lazy { ExternalDatabaseFetcher.get() }
    private val userLoginLiveData = MutableLiveData<Login>()

    val usernameTW get() = loginTW.usernameTW
    val passwordTW get() = loginTW.passwordTW

    val userLiveData: LiveData<LoginResult> =
        Transformations.switchMap(userLoginLiveData) { login ->
            univRepository.getUser(login)
    }

    fun inputValidation(): String? {
        val inputErrorText =
            when ("") {
                login.username -> "Логин не может быть пустым"
                login.password -> "Пароль не может быть пустым"
                else -> null
            }

        return inputErrorText
    }

    fun createUserDataHolderInstance(user: LoginResult) {
        UserDataHolder.initialize(user)
    }

    fun loadUser(isTeacher: Boolean) {
        login.isTeacher = isTeacher
        userLoginLiveData.value = login

        univAPI.fetchUser(login.username, login.password, isTeacher)
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
