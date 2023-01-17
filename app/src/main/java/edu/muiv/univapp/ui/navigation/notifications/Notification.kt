package edu.muiv.univapp.ui.navigation.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notification(
    @PrimaryKey
    val id          : String,
    var date        : String,
    var title       : String,
    var text        : String,
    var studentGroup: String
)
