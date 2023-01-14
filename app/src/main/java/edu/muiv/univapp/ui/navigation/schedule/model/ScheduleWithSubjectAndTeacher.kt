package edu.muiv.univapp.ui.navigation.schedule.model

import androidx.room.ColumnInfo
import java.util.*

data class ScheduleWithSubjectAndTeacher(
    // Schedule part
    val id          : UUID,
    val date        : String,
    val timeStart   : String,
    val timeEnd     : String,
    val roomNum     : Int,
    val type        : String,
    val teacherNotes: String,

    // Subject part
    @ColumnInfo(name = "subjectID")
    val subjectID   : UUID,
    val subjectName : String,
    val groupName   : String,

    // Teacher part
    @ColumnInfo(name = "teacherID")   val teacherID        : UUID,
    @ColumnInfo(name = "name")        val teacherName      : String,
    @ColumnInfo(name = "surname")     val teacherSurname   : String,
    @ColumnInfo(name = "patronymic")  val teacherPatronymic: String,
)
