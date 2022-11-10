package edu.muiv.univapp.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.login.Login
import edu.muiv.univapp.login.LoginResult

class UserViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val userLoginLiveData = MutableLiveData<Login>()


    var userLiveData: LiveData<LoginResult> =
        Transformations.switchMap(userLoginLiveData) { login ->
            univRepository.getUser(login)
    }

    fun loadUser(login: Login) {
        userLoginLiveData.value = login
    }

    fun addStudent(student: Student) {
        univRepository.addStudent(student)
    }

    fun addTeacher(teacher: Teacher) {
        univRepository.addTeacher(teacher)
    }
}