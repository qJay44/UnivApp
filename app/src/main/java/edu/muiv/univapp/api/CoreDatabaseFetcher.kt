package edu.muiv.univapp.api

import android.util.Base64
import android.util.Log
import edu.muiv.univapp.ui.login.Login
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.SubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import edu.muiv.univapp.utils.UserDataHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
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

    private fun encodeString(login: String, password: String): String {
        val byteArray = ("$login:$password").toByteArray(Charset.forName("UTF-8"))

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * @param login: object with user input.
     * @param callback: lambda callback receives status code as parameter.
     *
     * Response codes ->
     * 204: Response is OK but no content
     * 200: Response is OK
     * 503: Service is unavailable
     * 500: Unexpected fail
     */
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
                Log.e(TAG, "onFailure: User fetch fail", t)
                callback.invoke(500)
            }
        })
    }

    /**
     * @param group: the group that references in notification.
     * @param callback: lambda callback receives status code and notifications (or null).
     *
     * Response codes ->
     * 204: Response is OK but no content
     * 200: Response is OK
     * 503: Service is unavailable
     * 500: Unexpected fail
     */
    fun fetchNotifications(group: String, callback: (Map<Int, List<Notification>?>) -> Unit) {
        val request = coreDatabaseApi.fetchNotifications(group)
        request.enqueue(object : Callback<List<Notification>> {
            override fun onResponse(
                call: Call<List<Notification>>,
                response: Response<List<Notification>>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.isEmpty()) {
                        callback.invoke(mapOf(204 to null))
                        Log.i(TAG, "onResponse: response has no content (fetchNotifications)")
                    } else {
                        callback.invoke(mapOf(200 to responseBody))
                        Log.i(TAG, "onResponse: OK (fetchNotifications)")
                    }
                } else {
                    callback.invoke(mapOf(503 to null))
                    Log.w(TAG, "onResponse: responseBody is null (fetchNotifications)")
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                Log.e(TAG, "onFailure: Notifications fetch fail", t)
                callback.invoke(mapOf(500 to null))
            }
        })
    }

    /**
     * @param group: the group that references in schedule.
     * @param teacherId: the teacher's id that references in schedule.
     * @param callback: lambda callback receives status code and schedule (or null).
     *
     * Response codes ->
     * 204: Response is OK but no content
     * 200: Response is OK
     * 503: Service is unavailable
     * 500: Unexpected fail
     */
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

        request.enqueue(object : Callback<List<ScheduleWithSubjectAndTeacher>> {
            override fun onResponse(
                call: Call<List<ScheduleWithSubjectAndTeacher>>,
                response: Response<List<ScheduleWithSubjectAndTeacher>>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.isEmpty()) {
                        callback.invoke(mapOf(204 to null))
                        Log.i(TAG, "onResponse: response has no content (fetchSchedule)")
                    } else {
                        callback.invoke(mapOf(200 to responseBody))
                        Log.i(TAG, "onResponse: OK (fetchSchedule)")
                    }
                } else {
                    callback.invoke(mapOf(503 to null))
                    Log.w(TAG, "onResponse: responseBody is null (fetchSchedule)")
                }
            }

            override fun onFailure(call: Call<List<ScheduleWithSubjectAndTeacher>>, t: Throwable) {
                Log.e(TAG, "onFailure: Schedule fetch fail", t)
                callback.invoke(mapOf(500 to null))
            }
        })
    }

    /**
     * @param group: the group that references in subject.
     * @param callback: lambda callback receives status code and [SubjectAndTeacher] (or null).
     *
     * Response codes ->
     * 204: Response is OK but no content
     * 200: Response is OK
     * 503: Service is unavailable
     * 500: Unexpected fail
     */
    fun fetchProfileSubjects(group: String, callback: (Map<Int, List<SubjectAndTeacher>?>) -> Unit) {
        val request = coreDatabaseApi.fetchProfileSubjects(group)
        request.enqueue(object : Callback<List<SubjectAndTeacher>> {
            override fun onResponse(
                call: Call<List<SubjectAndTeacher>>,
                response: Response<List<SubjectAndTeacher>>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.isEmpty()) {
                        callback.invoke(mapOf(204 to null))
                        Log.i(TAG, "onResponse: response has no content (fetchProfileSubjects)")
                    } else {
                        callback.invoke(mapOf(200 to responseBody))
                        Log.i(TAG, "onResponse: OK (fetchProfileSubjects)")
                    }
                } else {
                    callback.invoke(mapOf(503 to null))
                    Log.w(TAG, "onResponse: responseBody is null (fetchProfileSubjects)")
                }
            }

            override fun onFailure(call: Call<List<SubjectAndTeacher>>, t: Throwable) {
                Log.e(TAG, "onFailure: ProfileSubjects fetch fail", t)
                callback.invoke(mapOf(500 to null))
            }
        })
    }
}
