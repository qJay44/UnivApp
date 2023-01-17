package edu.muiv.univapp.ui.navigation.schedule

enum class ScheduleWeekDay(val dayName: String) {
    MONDAY("ПОНЕДЕЛЬНИК"),
    TUESDAY("ВТОРНИК"),
    WEDNESDAY("СРЕДА"),
    THURSDAY("ЧЕТВЕРГ"),
    FRIDAY("ПЯТНИЦА"),
    SATURDAY("СУББОТА"),
    SUNDAY("ВОСКРЕСЕНЬЕ");

    companion object {
        fun getDayNameByIndex(index: Int): String = values()[index].dayName
    }
}
