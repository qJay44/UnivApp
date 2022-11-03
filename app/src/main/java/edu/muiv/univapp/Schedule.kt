package edu.muiv.univapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Schedule(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var date: String = "",
    var timeStart: String = "",
    var timeEnd: String = "",
    var subjectName: String = "",
    var roomNum: Int = 0
)
