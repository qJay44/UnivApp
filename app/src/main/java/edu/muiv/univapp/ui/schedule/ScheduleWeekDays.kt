package edu.muiv.univapp.ui.schedule

enum class ScheduleWeekDays(val dayName: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    companion object {
        fun getDayNameByIndex(index: Int): String = values()[index].dayName
    }
}