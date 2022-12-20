package edu.muiv.univapp.ui.navigation.profile

import androidx.room.ColumnInfo

data class SubjectAndTeacher(
    @ColumnInfo(name = "subjectName") val subjectName      : String,
    @ColumnInfo(name = "examType")    val subjectExamType  : String,
    @ColumnInfo(name = "name")        val teacherName      : String,
    @ColumnInfo(name = "surname")     val teacherSurname   : String,
    @ColumnInfo(name = "patronymic")  val teacherPatronymic: String,
)
