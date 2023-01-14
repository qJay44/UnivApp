package edu.muiv.univapp.api

import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

interface CoreDatabaseApi {

    @POST("v1/login/")
    fun fetchUser(@Body loginResponse: LoginResponse): Call<LoginResponse>

    @GET("v1/notifications/")
    fun fetchNotifications(@Query("group") group: String): Call<List<Notification>>

    @GET("v1/schedule/")
    fun fetchSchedule(@Query("group") group: String): Call<List<ScheduleWithSubjectAndTeacher>>

    @GET("v1/schedule/")
    fun fetchSchedule(@Query("teacherId") teacherId: UUID): Call<List<ScheduleWithSubjectAndTeacher>>
}
