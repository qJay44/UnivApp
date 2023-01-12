package edu.muiv.univapp.api

import android.util.Base64
import android.util.Log
import edu.muiv.univapp.ui.login.Login
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.utils.UserDataHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset


class ExternalDatabaseFetcher private constructor() {

    companion object {
        private const val TAG = "ExternalDatabaseFetcher"
        private var INSTANCE: ExternalDatabaseFetcher? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = ExternalDatabaseFetcher()
            }
        }

        fun get(): ExternalDatabaseFetcher {
            return INSTANCE ?: throw IllegalStateException("ExternalDatabaseFetcher must be initialized")
        }
    }

    private val externalDatabaseApi: ExternalDatabaseApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:3000/univ/hs/UnivAPI/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        externalDatabaseApi = retrofit.create(ExternalDatabaseApi::class.java)
    }

    private fun encodeString(login: String, password: String): String {
        val byteArray = ("$login:$password").toByteArray(Charset.forName("UTF-8"))

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * @param login: object with user input
     * @param callback: lambda callback receives status code as parameter
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

        val studentRequest = externalDatabaseApi.fetchUser(loginResponse)

        studentRequest.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.id == "") {
                        callback.invoke(204)
                    } else {
                        callback.invoke(200)
                        UserDataHolder.initialize(responseBody)
                    }
                } else {
                    callback.invoke(503)
                    Log.w(TAG, "onResponse: responseBody is null")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: User fetch fail", t)
                callback.invoke(500)
            }
        })
    }

    /**
     * @param group: the group that references in notification
     * @param callback: lambda callback receives status code and notifications (or null)
     *
     * Response codes ->
     * 204: Response is OK but no content
     * 200: Response is OK
     * 503: Service is unavailable
     * 500: Unexpected fail
     */
    fun fetchNotifications(group: String, callback: (Map<Int, List<Notification>?>) -> Unit) {
        val notificationsRequest = externalDatabaseApi.fetchNotifications(group)
        notificationsRequest.enqueue(object : Callback<List<Notification>> {
            override fun onResponse(
                call: Call<List<Notification>>,
                response: Response<List<Notification>>
            ) {
                val responseBody = response.body()
                if (responseBody != null) {
                    if (responseBody.isEmpty()) {
                        callback.invoke(mapOf(204 to null))
                    } else {
                        callback.invoke(mapOf(200 to responseBody))
                    }
                } else {
                    callback.invoke(mapOf(503 to null))
                    Log.w(TAG, "onResponse: responseBody is null")
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                Log.e(TAG, "onFailure: Notifications fetch fail", t)
                callback.invoke(mapOf(500 to null))
            }
        })
    }
}
