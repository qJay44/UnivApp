package edu.muiv.univapp.schedule

object TestDataBuilder {

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

    private val groupNames: Array<String> = arrayOf(
        "ИД", "ЭД"
    )

    private fun randInt(a: Int, b: Int) = (a..b).shuffled().last()

    fun createScheduleDay(scheduleDate: String, type: Int): List<Schedule> {
        val amount = randInt(1, 6)
        val scheduleList: MutableList<Schedule> = mutableListOf()
        val minStartIndex = timeStart.size - amount
        val startIndex = randInt(0, minStartIndex)
        val faculty =
            if (type == 0) subjectNames1
            else subjectNames2
        val groupFullName = "${groupNames[type]} 23.${randInt(1, 3)}/Б-${randInt(19, 22)}"

        for (i in 0 until amount) {
            val schedule = Schedule(
                date = scheduleDate,
                timeStart = timeStart[startIndex + i],
                timeEnd = timeEnd[startIndex + i],
                subjectName = faculty[(faculty.indices).shuffled().last()],
                roomNum = randInt(100, 525),
                studentGroup = groupFullName
            )
            scheduleList += schedule
        }

        return scheduleList
    }
}