package edu.muiv.univapp.ui.schedule

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class ScheduleUserNotes(
    @PrimaryKey val id: UUID,
    val scheduleID: UUID,
    val studentID: UUID,
    var notes: String?
)
