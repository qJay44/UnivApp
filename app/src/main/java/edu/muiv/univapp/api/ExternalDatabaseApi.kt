package edu.muiv.univapp.api

import edu.muiv.univapp.ui.navigation.notifications.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ExternalDatabaseApi {

    @POST("v1/login/")
    fun fetchUser(@Body loginResponse: LoginResponse): Call<LoginResponse>

    @GET("v1/notifications/")
    fun fetchNotifications(@Query("group") group: String): Call<List<Notification>>
}
