package edu.muiv.univapp.ui.navigation.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class ScheduleAttendance(
    @PrimaryKey val id     : UUID,
    val scheduleID         : UUID,
    val studentID          : UUID,
    val willAttend         : Boolean
)
