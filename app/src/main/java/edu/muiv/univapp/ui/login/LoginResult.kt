package edu.muiv.univapp.ui.login

import androidx.room.ColumnInfo
import java.util.UUID

data class LoginResult(
    @ColumnInfo(name = "id") val id: UUID,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "surname") val surname: String,
    @ColumnInfo(name = "patronymic") val patronymic: String,
    @ColumnInfo(name = "groupName") val groupName: String?
)