package edu.muiv.univapp.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Subject(
    @PrimaryKey val id: UUID,
    val subjectName: String,
    val groupName: String,
    val teacherID: UUID,
    val examType: String
)
