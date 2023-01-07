package edu.muiv.univapp.api

data class LoginResponse(
    val id        : String = "",
    val token     : String = "",
    val isTeacher : Boolean = false,
    val name      : String = "",
    val surname   : String = "",
    val patronymic: String = "",
    val groupName : String? = "",
    val course    : String? = "",
    val semester  : String? = ""
)
