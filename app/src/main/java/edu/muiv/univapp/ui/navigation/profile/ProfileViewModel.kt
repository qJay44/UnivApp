package edu.muiv.univapp.ui.navigation.profile

import androidx.lifecycle.*
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.FetchedListType
import edu.muiv.univapp.utils.UserDataHolder
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val univRepository = UnivRepository.get()
    private val user = UserDataHolder.get().user
    private val univApi by lazy { CoreDatabaseFetcher.get() }

    // LiveData variables //

    private val groupName = MutableLiveData<String>()
    private val _profileAttendance = MutableLiveData<UUID>()
    private val _subjectsFetched = MutableLiveData<Map<Int, List<SubjectAndTeacher>?>>()
    private val _profileAttendanceFetched = MutableLiveData<Map<Int, List<ProfileAttendance>?>>()

    ////////////////////////

    private var subjectsIdsNew: MutableList<String>? = null
    private var subjectsIdsOld: MutableList<String>? = null
    private var subjectsIdsToDelete: MutableList<String>? = null

    private var profileAttendanceIdsNew: MutableList<String>? = null
    private var profileAttendanceIdsOld: MutableList<String>? = null
    private var profileAttendanceIdsToDelete: MutableList<String>? = null

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

    private fun deleteSubjectsById() {
        univRepository.deleteSubjectsById(subjectsIdsToDelete!!)
    }

    private fun deleteProfileAttendanceById() {
        univRepository.deleteProfileAttendanceById(profileAttendanceIdsToDelete!!)
    }

    private fun findIdsToDeleteSubjects() {
        // To find deleted ids just compare old ones with new
        subjectsIdsToDelete = mutableListOf()
        for (oldId in subjectsIdsOld!!) {
            if (oldId !in subjectsIdsNew!!) {
                subjectsIdsToDelete!!.add(oldId)
            }
        }
        deleteSubjectsById()


        // Nullify lists to free some memory
        subjectsIdsNew = null
        subjectsIdsOld = null
        subjectsIdsToDelete = null
    }

    private fun findIdsToDeleteProfileAttendance() {
        // To find deleted ids just compare old ones with new
        profileAttendanceIdsToDelete = mutableListOf()
        for (oldId in profileAttendanceIdsOld!!) {
            if (oldId !in profileAttendanceIdsNew!!) {
                profileAttendanceIdsToDelete!!.add(oldId)
            }
        }
        deleteProfileAttendanceById()

        // Nullify lists to free some memory
        profileAttendanceIdsNew = null
        profileAttendanceIdsOld = null
        profileAttendanceIdsToDelete = null
    }

    fun createSubjectsIdsList(subjectAndTeacherList: List<SubjectAndTeacher>, type: Int) {
        viewModelScope.launch {
            when (type) {
                // The list from API call
                FetchedListType.NEW.type -> {
                    subjectsIdsNew = mutableListOf()
                    subjectAndTeacherList.forEach { subjectsIdsNew!!.add(it.subjectID) }

                    /** Find ids that were removed in the core database */
                    findIdsToDeleteSubjects()
                }
                // The list from the app database
                FetchedListType.OLD.type -> {
                    subjectsIdsOld = mutableListOf()
                    subjectAndTeacherList.forEach { subjectsIdsOld!!.add(it.subjectID) }

                    /** Try to get new [SubjectAndTeacher] */
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
                    profileAttendanceIdsNew = mutableListOf()
                    profileAttendanceList.forEach { profileAttendanceIdsNew!!.add(it.id) }

                    /** Find ids that were removed in the core database */
                    findIdsToDeleteProfileAttendance()
                }
                // The list from the app database
                FetchedListType.OLD.type -> {
                    profileAttendanceIdsOld = mutableListOf()
                    profileAttendanceList.forEach { profileAttendanceIdsOld!!.add(it.id) }

                    /** Try to get new [ProfileAttendance] */
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
