package edu.muiv.univapp.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.login.Login

class UserViewModel : ViewModel() {

    private val univRepository = UnivRepository.get()
    private val univUserLiveData = MutableLiveData<Login>()

    var userLiveData: LiveData<User> =
        Transformations.switchMap(univUserLiveData) { user ->
            univRepository.getUser(user)
        }

    fun loadUser(login: Login) {
        univUserLiveData.value = login
    }

    fun addUser(user: User) {
        univRepository.addUser(user)
    }
}