package edu.muiv.univapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Student(
    @PrimaryKey
    val id        : UUID,
    var name      : String,
    var surname   : String,
    var patronymic: String,
    var groupName : String,
    var course    : String,
    var semester  : String
)
