package edu.muiv.univapp.ui.navigation

import androidx.lifecycle.ViewModel
import edu.muiv.univapp.utils.UserDataHolder

class NavigationViewModel : ViewModel() {
    private val user = UserDataHolder.get().user

    val nameField get() = "${user.name}\n${user.surname}"
    val groupName get() = user.groupName
    val studyInfo get() = "${user.course} курс | ${user.semester} семестр"
    val userInfo  get() = user
}
