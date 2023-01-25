package edu.muiv.univapp.ui.navigation.profile

import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.TwoListsDifferenceString
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val univApi by lazy { CoreDatabaseFetcher.get() }
    private val listDiffSubjects by lazy { TwoListsDifferenceString() }
    private val listDiffScheduleAttendance by lazy { TwoListsDifferenceString() }
    private val univRepository = UnivRepository.get()
    private val user = UserDataHolder.get().user

    // LiveData variables //

    private val groupName = MutableLiveData<String>()
    private val _profileAttendance = MutableLiveData<UUID>()
    private val _subjectsFetched = MutableLiveData<Map<Int, List<SubjectAndTeacher>?>>()
    private val _profileAttendanceFetched = MutableLiveData<Map<Int, List<ProfileAttendance>?>>()

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
            univRepository.getProfileAttendance(userID)
        }

    val subjectAndTeacherList: LiveData<List<SubjectAndTeacher>> =
        Transformations.switchMap(groupName) { groupName ->
            univRepository.getSubjectsAndTeachers(groupName)
        }

    val fetchedSubjects: LiveData<Map<Int, List<SubjectAndTeacher>?>>
        get() = _subjectsFetched

    val fetchedProfileAttendance: LiveData<Map<Int, List<ProfileAttendance>?>>
        get() = _profileAttendanceFetched

    /////////////////////////////////////

    private fun fetchProfileSubjects() {
        if (UserDataHolder.isServerOnline) {
            user.groupName?.let {
                univApi.fetchProfileSubjects(it) { response ->
                    _subjectsFetched.value = response
                }
            }
        }
    }

    private fun fetchProfileAttendance() {
        if (UserDataHolder.isServerOnline) {
            univApi.fetchProfileAttendance(user.id.toString()) { response ->
                _profileAttendanceFetched.value = response
            }
        }
    }

    fun createSubjectsIdsList(subjectAndTeacherList: List<SubjectAndTeacher>, type: Int) {
        viewModelScope.launch {
            when (type) {
                // The list from API call
                FetchedListType.NEW.type -> {
                    listDiffSubjects.newList = subjectAndTeacherList.map { it.subjectID }
                    univRepository.deleteSubjectsById(listDiffSubjects.deleteList)

                }
                // The list from the app database
                FetchedListType.OLD.type -> {
                    listDiffSubjects.oldList = subjectAndTeacherList.map { it.subjectID }
                    fetchProfileSubjects()
                }
            }
        }
    }

    fun createProfileAttendanceIdsList(profileAttendanceList: List<ProfileAttendance>, type: Int) {
        viewModelScope.launch {
            when (type) {
                // The list from API call
                FetchedListType.NEW.type -> {
                    listDiffScheduleAttendance.newList = profileAttendanceList.map { it.id }
                    univRepository.deleteProfileAttendanceById(listDiffScheduleAttendance.deleteList)
                }
                // The list from the app database
                FetchedListType.OLD.type -> {
                    listDiffScheduleAttendance.oldList = profileAttendanceList.map { it.id }
                    fetchProfileAttendance()
                }
            }
        }
    }

    fun loadSubjects() {
        user.groupName?.let {
            groupName.value = it
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

    fun upsertSubject(subjectAndTeacherList: List<SubjectAndTeacher>) {
        univRepository.upsertSubject(subjectAndTeacherList)
    }

    fun upsertProfileAttendance(profileAttendanceList: List<ProfileAttendance>) {
        univRepository.upsertProfileAttendance(profileAttendanceList)
    }
}
