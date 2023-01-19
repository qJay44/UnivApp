package edu.muiv.univapp.ui.navigation.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfileAttendance(
    @PrimaryKey val id: String,
    val scheduleID: String,
    val userID: String,
    val visited: Boolean
)