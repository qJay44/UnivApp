package edu.muiv.univapp.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.login.utils.DatabaseTestDataBuilder

class LoginViewModel : ViewModel() {

    private val login = Login()
    private val loginTW = LoginTextWatcher(login)
    private val univRepository by lazy { UnivRepository.get() }
    private val univAPI by lazy { CoreDatabaseFetcher.get() }

    private val _responseStatusCode = MutableLiveData<Int>()

    val responseCode: LiveData<Int>
        get() = _responseStatusCode

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
            _responseStatusCode.value = statusCode
        }
    }

    fun addAll(amount: Int, tag: String) {
        with (DatabaseTestDataBuilder) {
            createAll(amount)

            for (student in studentList)
                univRepository.addStudent(student)

            for (teacher in teacherList)
                univRepository.addTeacher(teacher)

            for (subject1 in subject1List)
                univRepository.addSubject(subject1)

            for (subject2 in subject2List)
                univRepository.addSubject(subject2)

            for (schedule in scheduleList)
                univRepository.addSchedule(schedule)

            for (notification in notificationList)
                univRepository.addNotification(notification)

            for (profileAttendance in profileAttendanceList)
                univRepository.addProfileAttendance(profileAttendance)

            Log.w(tag, "Created new test data:\n" +
                    "Students: ${studentList.size}\n" +
                    "Teachers: ${teacherList.size}\n" +
                    "Subjects: ${subject1List.size + subject2List.size}\n" +
                    "Schedules: ${scheduleList.size}\n" +
                    "Notifications: ${notificationList.size}\n" +
                    "ProfileAttendances: ${profileAttendanceList.size}\n"
            )
        }
    }
}
