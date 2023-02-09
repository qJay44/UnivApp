package edu.muiv.univapp.ui.navigation.schedule.utils

enum class AttendanceStatus(val status: String?) {
    OFFLINE("(оффлайн режим)"),
    LATE("(слишком поздно)"),
    ALLOWED(null),
    EARLY("(слишком рано)")
}