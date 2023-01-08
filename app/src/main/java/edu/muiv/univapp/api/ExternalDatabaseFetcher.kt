package edu.muiv.univapp.api

import android.util.Base64
import android.util.Log
import edu.muiv.univapp.ui.login.Login
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
     * */
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
                    if (responseBody.id == "")
                        // Response is OK but no content
                        callback.invoke(204)
                    else {
                        // Response is OK
                        callback.invoke(200)
                        UserDataHolder.initialize(responseBody)
                    }
                } else {
                    // Service is unavailable
                    callback.invoke(503)
                    Log.w(TAG, "onResponse: responseBody is null")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: fail", t)
                // Unexpected error
                callback.invoke(500)
            }
        })
    }
}
