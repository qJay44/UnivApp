package edu.muiv.univapp.ui.navigation.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScheduleAttendance(
    @PrimaryKey
    val id        : String,
    val scheduleID: String,
    val studentID : String,
    val willAttend: Boolean
)
