package edu.muiv.univapp.ui.navigation.schedule.model

import androidx.room.ColumnInfo

data class ScheduleWithSubjectAndTeacher(
    // Schedule part
    val id          : String,
    val date        : String,
    val timeStart   : String,
    val timeEnd     : String,
    val roomNum     : Int,
    val type        : String,
    val teacherNotes: String,

    // Subject part
    @ColumnInfo(name = "subjectID")
    val subjectID   : String,
    val subjectName : String,
    val groupName   : String,

    // Teacher part
    @ColumnInfo(name = "teacherID")   val teacherID        : String,
    @ColumnInfo(name = "name")        val teacherName      : String,
    @ColumnInfo(name = "surname")     val teacherSurname   : String,
    @ColumnInfo(name = "patronymic")  val teacherPatronymic: String,
)
