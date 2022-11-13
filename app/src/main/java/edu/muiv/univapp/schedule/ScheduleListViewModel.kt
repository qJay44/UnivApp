package edu.muiv.univapp.schedule

import android.os.Build
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.login.LoginResult
import java.util.*

class ScheduleListViewModel : ViewModel() {

    private lateinit var user: LoginResult
    private val univRepository = UnivRepository.get()
    private val scheduleForStudent = MutableLiveData<String>()
    private val scheduleForTeacher = MutableLiveData<UUID>()

    // Primitive properties //

    val title: String
        get() = "${user.name} ${user.surname} ${user.groupName ?: ""}"

    val isTeacher: Boolean
        get() = user.groupName == null

    //////////////////////////

    // LiveData properties //

    val scheduleListLiveData = univRepository.getSchedule()

    val studentSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForStudent) { scheduleGroup ->
            univRepository.getScheduleForStudent(scheduleGroup)
        }

    val teacherSchedulesLiveData: LiveData<List<Schedule>> =
        Transformations.switchMap(scheduleForTeacher) { teacherID ->
            univRepository.getScheduleForTeacher(teacherID)
        }

    /////////////////////////

    // LiveData value setters //

    private fun loadScheduleForStudent(scheduleGroup: String) {
        scheduleForStudent.value = scheduleGroup
    }

    private fun loadScheduleForTeacher(teacherID: UUID) {
        scheduleForTeacher.value = teacherID
    }

    ////////////////////////////

    fun loadUser(args: Bundle?) {
        @Suppress("DEPRECATION")
        val unpackedId: UUID =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                args?.getSerializable("id", UUID::class.java)!!
            else
                args?.getSerializable("id") as UUID

        val groupName: String? = args.getString("groupName")

        if (groupName != null) {
            user = LoginResult(
                unpackedId,
                args.getString("name")!!,
                args.getString("surname")!!,
                groupName,
            )

            loadScheduleForStudent(user.groupName!!)
        } else {
            user = LoginResult(
                unpackedId,
                args.getString("name")!!,
                args.getString("surname")!!,
                null
            )

            loadScheduleForTeacher(user.id)
        }
    }

    fun addSchedule(schedule: Schedule) {
        univRepository.addSchedule(schedule)
    }
}
