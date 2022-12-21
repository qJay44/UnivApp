package edu.muiv.univapp.ui.navigation.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Schedule(
    @PrimaryKey
    val id          : UUID,
    var date        : String,
    var timeStart   : String,
    var timeEnd     : String,
    var roomNum     : Int,
    var type        : String,
    var teacherNotes: String,
    var subjectID   : UUID,
    var teacherID   : UUID
)
