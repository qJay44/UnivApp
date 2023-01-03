package edu.muiv.univapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ExternalDatabaseApi {

    @POST("v1/login/")
    fun fetchStudent(@Body loginResponse: LoginResponse): Call<LoginResponse>
}
