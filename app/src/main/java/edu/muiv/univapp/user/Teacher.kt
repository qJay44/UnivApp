package edu.muiv.univapp.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import edu.muiv.univapp.schedule.Schedule
import java.util.UUID

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userID"),
            onDelete = ForeignKey.CASCADE
    ),
        ForeignKey(
            entity = Schedule::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("scheduleID"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Teacher(
    @PrimaryKey
    val teacherID : UUID = UUID.randomUUID(),
    val name      : String = "",
    val surname   : String = "",
    @ColumnInfo(index = true)
    val scheduleID: UUID = UUID.randomUUID(),
    @ColumnInfo(index = true)
    val userID    : UUID = UUID.randomUUID()
)
