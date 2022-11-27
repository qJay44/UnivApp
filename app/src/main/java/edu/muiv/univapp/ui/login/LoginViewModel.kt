package edu.muiv.univapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.notifications.Notification
import edu.muiv.univapp.ui.profile.ProfileAttendance
import edu.muiv.univapp.ui.schedule.Schedule
import edu.muiv.univapp.user.Student
import edu.muiv.univapp.user.Subject
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder

class LoginViewModel : ViewModel() {

    private val login = Login()
    private val loginTW = LoginTextWatcher(login)
    private val univRepository = UnivRepository.get()
    private val userLoginLiveData = MutableLiveData<Login>()

    val usernameTW get() = loginTW.usernameTW
    val passwordTW get() = loginTW.passwordTW

    var userLiveData: LiveData<LoginResult> =
        Transformations.switchMap(userLoginLiveData) { login ->
            univRepository.getUser(login)
    }

    fun inputValidation(): String {
        val inputErrorText =
            when ("") {
                login.username -> "Login field can't be empty"
                login.password -> "Password field can't be empty"
                else -> ""
            }

        return inputErrorText
    }

    fun createUserDataHolderInstance(user: LoginResult) {
        UserDataHolder.initialize(user)
    }

    fun loadUser(isTeacher: Boolean) {
        login.isTeacher = isTeacher
        userLoginLiveData.value = login
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