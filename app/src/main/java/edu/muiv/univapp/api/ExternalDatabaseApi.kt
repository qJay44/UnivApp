package edu.muiv.univapp.api

import retrofit2.Call
import retrofit2.http.GET

interface ExternalDatabaseApi {

    // TODO: Add request
    @GET("request here")
    fun fetchStudents(): Call<ExternalDatabaseResponse>
}
