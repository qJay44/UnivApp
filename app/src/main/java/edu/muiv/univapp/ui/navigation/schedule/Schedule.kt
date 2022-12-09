package edu.muiv.univapp.ui.navigation.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Schedule(
    @PrimaryKey
    val id          : UUID,
    val date        : String,
    val timeStart   : String,
    val timeEnd     : String,
    val subjectName : String,
    val roomNum     : Int,
    val type        : String,
    val studentGroup: String,
    val teacherID   : UUID
)
