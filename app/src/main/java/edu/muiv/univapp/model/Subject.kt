package edu.muiv.univapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Subject(
    @PrimaryKey val id: String,
    val subjectName: String,
    val groupName: String,
    val teacherID: String,
    val examType: String
)
