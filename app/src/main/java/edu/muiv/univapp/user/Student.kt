package edu.muiv.univapp.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("userID"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Student(
    @PrimaryKey
    val studentID: UUID = UUID.randomUUID(),
    val name     : String,
    val surname  : String,
    val groupName: String,
    @ColumnInfo(index = true)
    val userID   : UUID = UUID.randomUUID()
)
