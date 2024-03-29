package edu.muiv.univapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Teacher(
    @PrimaryKey
    val id        : UUID,
    val name      : String,
    val surname   : String,
    val patronymic: String,
)
