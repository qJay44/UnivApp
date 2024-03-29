package edu.muiv.univapp.ui.navigation.profile

import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.TwoStringListsDifference
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val univApi by lazy { CoreDatabaseFetcher.get() }
    private val listDiffSubjects by lazy { TwoStringListsDifference() }
    private val listDiffScheduleAttendance by lazy { TwoStringListsDifference() }
    private val univRepository = UnivRepository.get()
    private val user = UserDataHolder.get().user

    // LiveData variables //

    private val groupName = MutableLiveData<String>()
    private val _profileAttendance = MutableLiveData<UUID>()
    private val _subjectsFetched = MutableLiveData<Map<StatusCode, List<SubjectAndTeacher>?>>()
    private val _profileAttendanceFetched = MutableLiveData<Map<StatusCode, List<ProfileAttendance>?>>()

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

    val fetchedSubjects: LiveData<Map<StatusCode, List<SubjectAndTeacher>?>>
        get() = _subjectsFetched

    val fetchedProfileAttendance: LiveData<Map<StatusCode, List<ProfileAttendance>?>>
        get() = _profileAttendanceFetched

    /////////////////////////////////////

    private fun fetchProfileSubjects() {
        // Only if online and didn't fetch yet
        if (UserDataHolder.isInternetAvailable && _subjectsFetched.value == null) {
            user.groupName?.let {
                univApi.fetchProfileSubjects(it) { response ->
                    _subjectsFetched.value = response
                }
            }
        }
    }

    private fun fetchProfileAttendance() {
        // Only if online and didn't fetch yet
        if (UserDataHolder.isInternetAvailable && _profileAttendanceFetched.value == null) {
            univApi.fetchProfileAttendance(user.id.toString()) { response ->
                _profileAttendanceFetched.value = response
            }
        }
    }

    fun createSubjectsIdsList(subjectAndTeacherList: List<SubjectAndTeacher>, type: FetchedListType) {
        viewModelScope.launch(Dispatchers.Default) {
            when (type) {
                // The list from API call
                FetchedListType.NEW -> {
                    listDiffSubjects.newList = subjectAndTeacherList.map { it.subjectID }

                    val diffLists = listDiffSubjects.compareLists()
                    val deleteList = diffLists["delete"]
                    val upsertList = subjectAndTeacherList.filter {
                        it.subjectID in diffLists["upsert"]!!
                    }

                    univRepository.deleteAndUpsertSubjects(deleteList!!, upsertList)
                }
                // The list from the app database
                FetchedListType.OLD -> {
                    listDiffSubjects.oldList = subjectAndTeacherList.map { it.subjectID }
                    fetchProfileSubjects()
                }
            }
        }
    }

    fun createProfileAttendanceIdsList(profileAttendanceList: List<ProfileAttendance>, type: FetchedListType) {
        viewModelScope.launch(Dispatchers.Default) {
            when (type) {
                // The list from API call
                FetchedListType.NEW -> {
                    listDiffScheduleAttendance.newList = profileAttendanceList.map { it.id }

                    val diffLists = listDiffScheduleAttendance.compareLists()
                    val deleteList = diffLists["delete"]
                    val upserteList = profileAttendanceList.filter {
                        it.id in diffLists["upsert"]!!
                    }

                    univRepository.deleteAndUpsertProfileAttendance(deleteList!!, upserteList)
                }
                // The list from the app database
                FetchedListType.OLD -> {
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
        visitAmount = 0
        scheduleAllSize = profileAttendanceList.size

        for (scheduleVisit in profileAttendanceList) {
            if (scheduleVisit.visited) visitAmount++
        }
    }
}
