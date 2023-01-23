package edu.muiv.univapp.api

data class ScheduleAttendanceForTeacherResponse(
    // Student part
    val studentID : String,
    val studentName   : String,
    val studentSurname: String,

    // ScheduleAttendance part
    val scheduleAttendanceId: String,
    val scheduleID: String,
    val willAttend: Boolean
)
