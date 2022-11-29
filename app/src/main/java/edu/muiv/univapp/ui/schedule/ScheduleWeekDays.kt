package edu.muiv.univapp.ui.schedule

enum class ScheduleWeekDays(val dayName: String) {
    MONDAY("Понедельник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Среда"),
    THURSDAY("Четверг"),
    FRIDAY("Пятница"),
    SATURDAY("Суббота"),
    SUNDAY("Воскресенье");

    companion object {
        fun getDayNameByIndex(index: Int): String = values()[index].dayName
    }
}