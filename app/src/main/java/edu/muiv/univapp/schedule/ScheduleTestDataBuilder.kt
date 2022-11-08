package edu.muiv.univapp.schedule

object ScheduleTestDataBuilder {

    private val timeStart: Array<String> = arrayOf(
        "08:20", "10:00", "11:40", "13:45", "15:25", "17:05"
    )

    private val timeEnd: Array<String> = arrayOf(
        "09:50", "11:30", "13:10", "15:15", "16:55", "18:35"
    )

    private val subjectNames1: Array<String> = arrayOf(
        "Адаптация типовых конфигураций корпоративных информационных систем",
        "Библиотеки стандартных подсистем",
        "Информационная безопасность",
        "Информационные системы и технологии",
        "Обмен данными в корпоративных информационных системах",
        "Реплицированные распределенные хранилища данных",
        "Управление данными в корпоративных информационных системах",
        "Цифровой маркетинг"
    )

    private val subjectNames2: Array<String> = arrayOf(
        "Теория бухгалтерского учета",
        "Налоги и налогообложение",
        "Бухгалтерский финансовый учет",
        "Бухгалтерский управленческий учет",
        "Лабораторный практикум по бухгалтерскому учету",
        "Бухгалтерские информационные системы",
        "Бухгалтерская (финансовая) отчетность",
        "Антикризисное управление",
        "Аудит",
        "Комплексный экономический анализ хозяйственной деятельности"
    )

    private val groupNames1: Array<String> = arrayOf(
        "ИД 23.1/Б1-19",
        "ИД 23.2/Б1-19",
        "ИД 23.3/Б1-19",
    )

    private val groupNames2: Array<String> = arrayOf(
        "ЭД 23.1/Б1-19",
        "ЭД 23.2/Б1-19",
        "ЭД 23.3/Б1-19",
    )

    private fun randInt(a: Int, b: Int) = (a..b).shuffled().last()

    fun createScheduleDay(scheduleDate: String): List<Schedule> {
        val amount = randInt(1, 6)
        val scheduleList: MutableList<Schedule> = mutableListOf()
        val maxStartIndex = timeStart.size - amount
        val startIndex = randInt(0, maxStartIndex)
        val groupNameIndex = randInt(0, 1)
        val faculty = if (groupNameIndex == 0) subjectNames1 else subjectNames2
        val groupName = if (groupNameIndex == 0) groupNames1 else groupNames2

        for (i in 0 until amount) {
            val schedule = Schedule(
                date = scheduleDate,
                timeStart = timeStart[startIndex + i],
                timeEnd = timeEnd[startIndex + i],
                subjectName = faculty[(faculty.indices).shuffled().last()],
                roomNum = randInt(100, 525),
                studentGroup = groupName[(groupName.indices).shuffled().last()]
            )

            scheduleList += schedule
        }

        return scheduleList
    }
}