package edu.muiv.univapp.ui.navigation.profile

import androidx.room.ColumnInfo

data class SubjectAndTeacher(
    @ColumnInfo(name = "id")          val subjectID        : String,
    @ColumnInfo(name = "subjectName") val subjectName      : String,
    @ColumnInfo(name = "groupName")   val subjectGroupName : String,
    @ColumnInfo(name = "examType")    val subjectExamType  : String,
    @ColumnInfo(name = "teacherID")   val teacherID        : String,
    @ColumnInfo(name = "name")        val teacherName      : String,
    @ColumnInfo(name = "surname")     val teacherSurname   : String,
    @ColumnInfo(name = "patronymic")  val teacherPatronymic: String,
)
