package edu.muiv.univapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.user.Subject
import edu.muiv.univapp.user.Teacher
import edu.muiv.univapp.user.UserDataHolder
import java.util.UUID

class ProfileListViewModel : ViewModel() {
    private val repository = UnivRepository.get()
    private val user = UserDataHolder.get().user

    // LiveData variables //

    private val _subjects = MutableLiveData<String>()
    private val _teachers = MutableLiveData<Array<UUID>>()
    private val _profileAttendance = MutableLiveData<UUID>()
    val teachersById = MutableLiveData<Map<UUID, Teacher>>()

    ////////////////////////

    var scheduleAllSize = 0
    var visitAmount = 0

    val attendanceAmount: String
        get() = " $visitAmount / $scheduleAllSize"

    val attendancePercent: String
        get() {
            return try {
                " ${visitAmount / scheduleAllSize * 100}%"
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
}