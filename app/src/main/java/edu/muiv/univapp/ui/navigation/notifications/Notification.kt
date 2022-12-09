package edu.muiv.univapp.ui.navigation.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Notification(
    @PrimaryKey val id: UUID,
    var date: String,
    var title: String,
    var text: String
)
