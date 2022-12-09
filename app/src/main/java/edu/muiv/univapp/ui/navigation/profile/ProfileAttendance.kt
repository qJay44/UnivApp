package edu.muiv.univapp.ui.navigation.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class ProfileAttendance(
    @PrimaryKey val id: UUID,
    val scheduleID: UUID,
    val userID: UUID,
    val visited: Boolean
)