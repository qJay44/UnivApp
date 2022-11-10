package edu.muiv.univapp.user

import androidx.room.Embedded
import androidx.room.Relation
import edu.muiv.univapp.schedule.Schedule

data class TeacherWithSchedules(
    @Embedded val teacher: Teacher,
    @Relation(
        parentColumn = "id",
        entityColumn = "teacherID"
    )
    val schedules: List<Schedule>
)
