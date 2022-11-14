package edu.muiv.univapp.login

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.user.Student
import edu.muiv.univapp.user.Teacher

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

    fun getUserBundle(user: LoginResult): Bundle {
        val bundle = Bundle().apply {
            putSerializable("id", user.id)
            putString("name", user.name)
            putString("surname", user.surname)
            putString("groupName", user.groupName)
        }

        return bundle
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
}