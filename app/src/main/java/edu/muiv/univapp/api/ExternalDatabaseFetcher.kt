package edu.muiv.univapp.api

import android.util.Base64
import android.util.Log
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

//    interface Callbacks {
//        fun onLoginResponse()
//        fun onLoginFailure()
//    }

    private val externalDatabaseApi: ExternalDatabaseApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:3000/univ/hs/UnivAPI/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        externalDatabaseApi = retrofit.create(ExternalDatabaseApi::class.java)
    }

    private fun createAuthToken(login: String, password: String): String {
        val byteArray = ("$login:$password").toByteArray(Charset.forName("UTF-8"))

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun fetchUser(login: String, password: String, isTeacher: Boolean) {

        // POST body
        val loginResponse = LoginResponse(
            token = createAuthToken(login, password),
            isTeacher = isTeacher
        )

        val studentRequest = externalDatabaseApi.fetchUser(loginResponse)

        studentRequest.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                Log.i(TAG, "onResponse: ${responseBody?.id}")
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: fail", t)
            }
        })
    }
}
