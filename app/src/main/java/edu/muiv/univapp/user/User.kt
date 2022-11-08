package edu.muiv.univapp.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class User(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val login       : String = "",
    val password    : String = "",
    val name        : String = "",
    val surname     : String = "",
    val userGroup   : String = "",
    val studentGroup: String? = null
)