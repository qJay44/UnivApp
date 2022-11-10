package edu.muiv.univapp.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Schedule(
    @PrimaryKey
    val scheduleID  : UUID,
    val date        : String,
    val timeStart   : String,
    val timeEnd     : String,
    val subjectName : String,
    val roomNum     : Int,
    val studentGroup: String,
    val teacherID   : UUID
)
