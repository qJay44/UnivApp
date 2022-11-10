package edu.muiv.univapp.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Student(
    @PrimaryKey
    val id       : UUID,
    val name     : String,
    val surname  : String,
    val login    : String,
    val password : String,
    val groupName: String
)
