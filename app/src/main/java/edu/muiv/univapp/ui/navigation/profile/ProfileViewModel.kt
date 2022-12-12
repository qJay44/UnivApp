package edu.muiv.univapp.ui.navigation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.ui.navigation.schedule.model.Subject
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val repository = UnivRepository.get()
    private val user = UserDataHolder.get().user

    // LiveData variables //

    private val _subjects = MutableLiveData<String>()
    private val _teachers = MutableLiveData<Array<UUID>>()
    private val _profileAttendance = MutableLiveData<UUID>()

    ////////////////////////

    private var scheduleAllSize = 0
    private var visitAmount = 0

    val attendanceAmount: String
        get() = " $visitAmount / $scheduleAllSize"

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

    val subjects: LiveData<List<Subject>> =
        Transformations.switchMap(_subjects) { groupName ->
            repository.getSubjectsByGroupName(groupName)
        }

    val teachers: LiveData<Array<Teacher>> =
        Transformations.switchMap(_teachers) { IDs ->
            repository.getTeachersByIDs(IDs)
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

    fun loadSubjectTeachers(subjects: List<Subject>) {
        val teacherIDs: Array<UUID> = Array(subjects.size) {
            subjects[it].teacherID
        }
        _teachers.value = teacherIDs
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
