package edu.muiv.univapp.api

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
import java.util.UUID

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

    fun fetchUser(login: Login, callback: (StatusCode) -> Unit) {

        // POST body
        val loginResponse = LoginResponse(
            token = Encryptor.encrypt("${login.username}:${login.password}"),
            isTeacher = login.isTeacher
        )
        Log.i(TAG, "fetchUser: token: ${loginResponse.token}")

        val request = coreDatabaseApi.fetchUser(loginResponse)
        request.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.id == "") {
                        callback.invoke(StatusCode.NO_CONTENT)
                        Log.i(TAG, "onResponse: response has no content (fetchUser)")
                    } else {
                        callback.invoke(StatusCode.OK)
                        UserDataHolder.initialize(responseBody)
                        Log.i(TAG, "onResponse: OK (fetchUser)")
                    }
                } else {
                    callback.invoke(StatusCode.SERVICE_UNAVAILABLE)
                    Log.w(TAG, "onResponse: responseBody is null (fetchUser)")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: fetch fail (fetchUser)", t)
                callback.invoke(StatusCode.INTERNAL_SERVER_ERROR)
            }
        })
    }

    //================== GET requests ==================//

    fun fetchNotifications(group: String, callback: (Map<StatusCode, List<Notification>?>) -> Unit) {
        val request = coreDatabaseApi.fetchNotifications(group)
        request.enqueue(DefaultGetRequest<List<Notification>>(
            callback, true, "fetchNotification"))
    }

    fun fetchSchedule(
        group: String? = null,
        teacherId: UUID? = null,
        callback: (Map<StatusCode, List<ScheduleWithSubjectAndTeacher>?>) -> Unit
    ) {
        val request = if (teacherId == null) {
            coreDatabaseApi.fetchSchedule(group!!)
        } else {
            coreDatabaseApi.fetchSchedule(teacherId)
        }

        request.enqueue(DefaultGetRequest<List<ScheduleWithSubjectAndTeacher>>(
            callback, true, "fetchSchedule"))
    }

    fun fetchProfileSubjects(group: String, callback: (Map<StatusCode, List<SubjectAndTeacher>?>) -> Unit) {
        val request = coreDatabaseApi.fetchProfileSubjects(group)
        request.enqueue(DefaultGetRequest<List<SubjectAndTeacher>>(
            callback, true, "fetchProfileSubjects"))
    }

    fun fetchProfileAttendance(userId: String, callback: (Map<StatusCode, List<ProfileAttendance>?>) -> Unit) {
        val request = coreDatabaseApi.fetchProfileAttendance(userId)
        request.enqueue(DefaultGetRequest<List<ProfileAttendance>>(
            callback, true, "fetchProfileAttendance"))
    }

    fun fetchScheduleAttendanceForStudent(
        scheduleId: String,
        studentId: String,
        callback: (Map<StatusCode, ScheduleAttendance?>) -> Unit
    ) {
        val params = hashMapOf("scheduleId" to scheduleId, "studentId" to studentId)
        val request = coreDatabaseApi.fetchScheduleAttendanceForStudent(params)
        request.enqueue(DefaultGetRequest<ScheduleAttendance>(
            callback, false, "fetchScheduleAttendanceForStudent"))
    }

    fun fetchScheduleAttendanceForTeacher(
        scheduleId: String,
        callback: (Map<StatusCode, List<ScheduleAttendanceForTeacherResponse>?>) -> Unit
    ) {
        val request = coreDatabaseApi.fetchScheduleAttendanceForTeacher(scheduleId)
        request.enqueue(DefaultGetRequest<List<ScheduleAttendanceForTeacherResponse>>(
            callback, true, "fetchScheduleAttendanceForTeacher"))
    }

    //==================================================//
    //================== PUT requests ==================//

    fun updateScheduleAttendance(scheduleAttendance: ScheduleAttendance, callback: (StatusCode) -> Unit) {
        val request = coreDatabaseApi.updateScheduleAttendance(scheduleAttendance)
        request.enqueue(object : Callback<Unit> {
            override fun onResponse(
                call: Call<Unit>,
                response: Response<Unit>
            ) {
                Log.i(TAG, "onResponse: OK (updateScheduleAttendance)")
                callback.invoke(StatusCode.OK)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "onFailure: Schedule attendance update fail", t)
                callback.invoke(StatusCode.INTERNAL_SERVER_ERROR)
            }
        })
    }

    fun updateSchedule(schedule: Schedule, callback: (StatusCode) -> Unit) {
        val request = coreDatabaseApi.updateSchedule(schedule)
        request.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.i(TAG, "onResponse: OK (updateSchedule)")
                callback.invoke(StatusCode.OK)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "onFailure: Schedule update fail", t)
                callback.invoke(StatusCode.INTERNAL_SERVER_ERROR)
            }
        })
    }

    //==================================================//

    private class DefaultGetRequest<T> (
        private val callback: (Map<StatusCode, T?>) -> Unit,
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
                    callback.invoke(mapOf(StatusCode.OK to responseBody))
                    Log.i(TAG, "onResponse: OK ($funcName)")
                } else {
                    callback.invoke(mapOf(StatusCode.NO_CONTENT to null))
                    Log.i(TAG, "onResponse: response has no content ($funcName)")
                }
            } else {
                callback.invoke(mapOf(StatusCode.SERVICE_UNAVAILABLE to null))
                Log.w(TAG, "onResponse: responseBody is null ($funcName)")
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Log.e(TAG, "onFailure: ($funcName)", t)
            callback.invoke(mapOf(StatusCode.INTERNAL_SERVER_ERROR to null))
        }
    }

    private data class GenericResponse(val id: String)
}
