package edu.muiv.univapp.ui.navigation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.model.SubjectAndTeacher
import edu.muiv.univapp.utils.UserDataHolder
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val repository = UnivRepository.get()
    private val user = UserDataHolder.get().user

    // LiveData variables //

    private val _subjects = MutableLiveData<String>()
    private val _profileAttendance = MutableLiveData<UUID>()

    ////////////////////////

    private var scheduleAllSize = 0
    private var visitAmount = 0

    val attendanceAmount: String
        get() = " $visitAmount / $scheduleAllSize пар"

    val attendancePercent: String
        get() {
            return try {
                " ${(visitAmount.toFloat() / scheduleAllSize * 100).toInt()}%"
            } catch (e: ArithmeticException) {
                " 100%"
            }
        }

    // LiveData repository observables //

    val profileAttendance: LiveData<List<ProfileAttendance>> =
        Transformations.switchMap(_profileAttendance) { userID ->
            repository.getProfileAttendance(userID)
        }

    val subjectAndTeacherList: LiveData<List<SubjectAndTeacher>> =
        Transformations.switchMap(_subjects) { groupName ->
            repository.getSubjectsAndTeachers(groupName)
        }

    /////////////////////////////////////

    fun loadSubjects() {
        user.groupName?.let {
            _subjects.value = it
        }
    }

    fun loadProfileAttendance() {
        _profileAttendance.value = user.id
    }

    fun loadProfileProperties(profileAttendanceList: List<ProfileAttendance>) {
        scheduleAllSize = profileAttendanceList.size

        for (scheduleVisit in profileAttendanceList) {
            if (scheduleVisit.visited) visitAmount++
        }
    }

    fun resetVisitAmount() {
        visitAmount = 0
    }
}
