package edu.muiv.univapp.login

import androidx.room.ColumnInfo
import java.util.UUID

data class LoginResult(
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "surname") val surname: String,
    @ColumnInfo(name = "groupName") val groupName: String?
)