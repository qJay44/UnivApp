package edu.muiv.univapp.ui.navigation.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.user.Student
import java.util.*

class StudentsWillAttendDialogFragmentVM : ViewModel() {
    private val univRepository = UnivRepository.get()
    private val scheduleIdLiveData = MutableLiveData<UUID>()

    val studentsWillAttend: LiveData<List<Student>> =
        Transformations.switchMap(scheduleIdLiveData) { id ->
            univRepository.getWillAttendStudents(id)
        }

    fun loadStudents(scheduleID: UUID) {
        scheduleIdLiveData.value = scheduleID
    }
}
