package edu.muiv.univapp.api

import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.profile.SubjectAndTeacher
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.ScheduleWithSubjectAndTeacher
import retrofit2.Call
import retrofit2.http.*

interface CoreDatabaseApi {

    @POST("v1/login/")
    fun fetchUser(@Body loginResponse: LoginResponse): Call<LoginResponse>

    @GET("v1/notifications/")
    fun fetchNotifications(@Query("group") group: String): Call<List<Notification>>

    @GET("v1/schedule/")
    fun fetchSchedule(@QueryMap params: Map<String, String?>): Call<List<ScheduleWithSubjectAndTeacher>>

    @GET("v1/profile/subjects/")
    fun fetchProfileSubjects(@Query("groupName") groupName: String): Call<List<SubjectAndTeacher>>

    @GET("v1/profile/attendance/")
    fun fetchProfileAttendance(@Query("userId") userId: String): Call<List<ProfileAttendance>>

    @GET("v1/schedule/attendance/student")
    fun fetchScheduleAttendanceForStudent(@QueryMap params: Map<String, String>): Call<ScheduleAttendance>

    @GET("v1/schedule/attendance/teacher")
    fun fetchScheduleAttendanceForTeacher(@Query("scheduleId") scheduleId: String): Call<List<ScheduleAttendanceForTeacherResponse>>

    @PUT("v1/schedule/attendance/update")
    fun updateScheduleAttendance(@Body scheduleAttendance: ScheduleAttendance): Call<Unit>

    @PUT("v1/schedule/update")
    fun updateSchedule(@Body schedule: Schedule): Call<Unit>
}
