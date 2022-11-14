package edu.muiv.univapp.login

data class Login(
    var username: String = "",
    var password: String = "",
    var isTeacher: Boolean = false
)