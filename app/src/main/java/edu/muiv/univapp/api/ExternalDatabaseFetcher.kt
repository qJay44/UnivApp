package edu.muiv.univapp.api

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExternalDatabaseFetcher {

    companion object {
        private const val TAG = "ExternalDatabaseFetcher"
    }

    private val externalDatabaseApi: ExternalDatabaseApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/muiv/hs/PrettyAPI/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        externalDatabaseApi = retrofit.create(ExternalDatabaseApi::class.java)
    }

    fun fetchStudent(login: String, password: String) {
        val loginResponse = LoginResponse().apply {
            this.login = login
            this.password = password
        }

        val studentRequest = externalDatabaseApi.fetchStudent(loginResponse)

        studentRequest.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val responseBody = response.body()
                responseBody?.let {
                    Log.i(TAG, "onResponse: $responseBody")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: fail", t)
            }
        })
    }
}
