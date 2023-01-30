package edu.muiv.univapp.api

import android.util.Base64
import android.util.Log
import edu.muiv.univapp.ui.login.Login
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.profile.SubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.utils.UserDataHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
import java.util.UUID

/**
* Response codes ->
* 204: Response is OK but no content
* 200: Response is OK
* 503: Service is unavailable
* 500: Unexpected fail
*/

class CoreDatabaseFetcher private constructor() {

    companion object {
        private const val TAG = "CoreDatabaseFetcher"
        private var INSTANCE: CoreDatabaseFetcher? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = CoreDatabaseFetcher()
            }
        }

        fun get(): CoreDatabaseFetcher {
            return INSTANCE ?: throw IllegalStateException("CoreDatabaseFetcher must be initialized")
        }
    }

    private val coreDatabaseApi: CoreDatabaseApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:3000/univ/hs/UnivAPI/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        coreDatabaseApi = retrofit.create(CoreDatabaseApi::class.java)
    }

    private fun encodeString(login: String, password: String): String {
        val byteArray = ("$login:$password").toByteArray(Charset.forName("UTF-8"))

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun fetchUser(login: Login, callback: (Int) -> Unit) {

        // POST body
        val loginResponse = LoginResponse(
            token = encodeString(login.username, login.password),
            isTeacher = login.isTeacher
        )

        val request = coreDatabaseApi.fetchUser(loginResponse)
        request.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.id == "") {
                        callback.invoke(204)
                        Log.i(TAG, "onResponse: response has no content (fetchUser)")
                    } else {
                        callback.invoke(200)
                        UserDataHolder.initialize(responseBody)
                        Log.i(TAG, "onResponse: OK (fetchUser)")
                    }
                } else {
                    callback.invoke(503)
                    Log.w(TAG, "onResponse: responseBody is null (fetchUser)")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: fetch fail (fetchUser)", t)
                callback.invoke(500)
            }
        })
    }

    //================== GET requests ==================//

    fun fetchNotifications(group: String, callback: (Map<Int, List<Notification>?>) -> Unit) {
        val request = coreDatabaseApi.fetchNotifications(group)
        request.enqueue(DefaultGetRequest<List<Notification>>(
            callback, true, "fetchNotification"))
    }

    fun fetchSchedule(
        group: String? = null,
        teacherId: UUID? = null,
        callback: (Map<Int, List<ScheduleWithSubjectAndTeacher>?>) -> Unit
    ) {
        val request = if (teacherId == null) {
            coreDatabaseApi.fetchSchedule(group!!)
        } else {
            coreDatabaseApi.fetchSchedule(teacherId)
        }

        request.enqueue(DefaultGetRequest<List<ScheduleWithSubjectAndTeacher>>(
            callback, true, "fetchSchedule"))
    }

    fun fetchProfileSubjects(group: String, callback: (Map<Int, List<SubjectAndTeacher>?>) -> Unit) {
        val request = coreDatabaseApi.fetchProfileSubjects(group)
        request.enqueue(DefaultGetRequest<List<SubjectAndTeacher>>(
            callback, true, "fetchProfileSubjects"))
    }

    fun fetchProfileAttendance(userId: String, callback: (Map<Int, List<ProfileAttendance>?>) -> Unit) {
        val request = coreDatabaseApi.fetchProfileAttendance(userId)
        request.enqueue(DefaultGetRequest<List<ProfileAttendance>>(
            callback, true, "fetchProfileAttendance"))
    }

    fun fetchScheduleAttendanceForStudent(
        scheduleId: String,
        studentId: String,
        callback: (Map<Int, ScheduleAttendance?>) -> Unit
    ) {
        val params = hashMapOf("scheduleId" to scheduleId, "studentId" to studentId)
        val request = coreDatabaseApi.fetchScheduleAttendanceForStudent(params)
        request.enqueue(DefaultGetRequest<ScheduleAttendance>(
            callback, false, "fetchScheduleAttendanceForStudent"))
    }

    fun fetchScheduleAttendanceForTeacher(
        scheduleId: String,
        callback: (Map<Int, List<ScheduleAttendanceForTeacherResponse>?>) -> Unit
    ) {
        val request = coreDatabaseApi.fetchScheduleAttendanceForTeacher(scheduleId)
        request.enqueue(DefaultGetRequest<List<ScheduleAttendanceForTeacherResponse>>(
            callback, true, "fetchScheduleAttendanceForTeacher"))
    }

    //==================================================//
    //================== PUT requests ==================//

    fun updateScheduleAttendance(scheduleAttendance: ScheduleAttendance, callback: (Int) -> Unit) {
        val request = coreDatabaseApi.updateScheduleAttendance(scheduleAttendance)
        request.enqueue(object : Callback<ScheduleAttendance> {
            override fun onResponse(
                call: Call<ScheduleAttendance>,
                response: Response<ScheduleAttendance>
            ) {
                Log.i(TAG, "onResponse: OK (updateScheduleAttendance)")
                callback.invoke(response.code())
            }

            override fun onFailure(call: Call<ScheduleAttendance>, t: Throwable) {
                Log.e(TAG, "onFailure: Schedule attendance update fail", t)
                callback.invoke(500)
            }
        })
    }

    fun updateSchedule(schedule: Schedule, callback: (Int) -> Unit) {
        val request = coreDatabaseApi.updateSchedule(schedule)
        request.enqueue(object : Callback<Schedule> {
            override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
                Log.i(TAG, "onResponse: OK (updateSchedule)")
                callback.invoke(response.code())
            }

            override fun onFailure(call: Call<Schedule>, t: Throwable) {
                Log.e(TAG, "onFailure: Schedule update fail", t)
                callback.invoke(500)
            }
        })
    }

    //==================================================//

    private class DefaultGetRequest<T> (
        private val callback: (Map<Int, T?>) -> Unit,
        private val isList  : Boolean,
        private val funcName: String
        ) : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val responseBody = response.body()
            if (responseBody != null) {
                val hasContent = if (isList) {
                    val useAsList = responseBody as ArrayList<*>
                    useAsList.isNotEmpty()
                } else {
                    val useAsObject = responseBody as GenericResponse
                    useAsObject.id != ""
                }

                if (hasContent) {
                    callback.invoke(mapOf(200 to responseBody))
                    Log.i(TAG, "onResponse: OK ($funcName)")
                } else {
                    callback.invoke(mapOf(204 to null))
                    Log.i(TAG, "onResponse: response has no content ($funcName)")
                }
            } else {
                callback.invoke(mapOf(503 to null))
                Log.w(TAG, "onResponse: responseBody is null ($funcName)")
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.e(TAG, "onFailure: ($funcName)", t)
            callback.invoke(mapOf(500 to null))
        }
    }

    private data class GenericResponse(val id: String)
}
