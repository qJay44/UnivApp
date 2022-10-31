package edu.muiv.univapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Schedule(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var date: Date = Date(),
    var time: String = "",
    var subjectName: String = "",
    var roomNum: Int = 0
)
