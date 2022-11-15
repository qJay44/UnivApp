package edu.muiv.univapp.user

import android.util.Log
import edu.muiv.univapp.ui.schedule.Schedule
import java.util.UUID

object DatabaseTestDataBuilder {

    private const val TAG = "DatabaseTestDataBuilder"

    private val userGroups: Array<String> = arrayOf(
        "Student", "Teacher"
    )

    private val names: Array<String> = arrayOf(
        "Aaron",
        "Abbey",
        "Abbie",
        "Abby",
        "Abdul",
        "Abe",
        "Abel",
        "Abigail",
        "Abraham",
        "Abram",
        "Ada",
        "Adah",
        "Adalberto",
        "Adaline",
    )

    @Suppress("SpellCheckingInspection")
    private val surnames: Array<String> = arrayOf(
        "Anderson",
        "Ashwoon",
        "Aikin",
        "Bateman",
        "Bongard",
        "Bowers",
        "Boyd",
        "Cannon",
        "Cast",
        "Deitz",
        "Dewalt",
        "Ebner",
        "Frick",
        "Hancock",
        "Haworth",
        "Hesch",
        "Hoffman",
        "Kassing",
        "Knutson",
        "Lawless",
        "Lawicki",
        "Mccord",
        "McCormack",
        "Miller",
        "Myers",
        "Nugent",
        "Ortiz",
        "Orwig",
        "Ory",
        "Paiser",
        "Pak",
        "Pettigrew",
        "Quinn",
        "Quizoz",
        "Ramachandran",
        "Resnick",
        "Sagar",
        "Schickowski",
        "Schiebel",
        "Sellon",
        "Severson",
        "Shaffer",
        "Solberg",
        "Soloman",
        "Sonderling",
        "Soukup",
        "Soulis",
        "Stahl",
        "Sweeney",
        "Tandy",
        "Trebil",
        "Trusela",
        "Trussel",
        "Turco",
        "Uddin",
        "Uflan",
        "Ulrich",
        "Upson",
        "Vader",
        "Vail",
        "Valente",
        "Van Zandt",
        "Vanderpoel",
        "Ventotla",
        "Vogal",
        "Wagle",
        "Wagner",
        "Wakefield",
        "Weinstein"
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

    val studentList: MutableList<Student> = mutableListOf()
    val teacherList: MutableList<Teacher> = mutableListOf()
    val scheduleList: MutableList<Schedule> = mutableListOf()

    private fun randInt(a: Int, b: Int) = (a..b).shuffled().last()

    private fun createScheduleDay(scheduleDate: String) {
        val amount = randInt(1, 6)
        val maxStartIndex = timeStart.size - amount
        val startIndex = randInt(0, maxStartIndex)
        val groupNameIndex = randInt(0, 1)
        val faculty = if (groupNameIndex == 0) subjectNames1 else subjectNames2
        val groupName = if (groupNameIndex == 0) groupNames1 else groupNames2

        for (i in 0 until amount) {
            val schedule = Schedule(
                id = UUID.randomUUID(),
                date = scheduleDate,
                timeStart = timeStart[startIndex + i],
                timeEnd = timeEnd[startIndex + i],
                subjectName = faculty[(faculty.indices).shuffled().last()],
                roomNum = randInt(100, 525),
                studentGroup = groupName[(groupName.indices).shuffled().last()],
                teacherID = teacherList[(teacherList.indices).shuffled().last()].id
            )

            scheduleList += schedule
        }
    }

    private fun randArrayElement(arr: Array<String>) = arr[(arr.indices).shuffled().last()]

    fun createAll(amount: Int) {

        for (i in 0 until amount) {
            val isHalf = i > amount / 2 - 1

            when (if (isHalf) userGroups[0] else userGroups[1]) {
                "Student" -> {
                    val student =
                        Student(
                            id = UUID.randomUUID(),
                            name = randArrayElement(names),
                            surname = randArrayElement(surnames),
                            login = "stud${studentList.size + 1}",
                            password = "1",
                            groupName = if (isHalf) groupNames1[0] else groupNames2[1],
                        )
                    studentList += student
                }
                "Teacher" -> {
                    val teacher =
                        Teacher(
                            id = UUID.randomUUID(),
                            name = randArrayElement(names),
                            surname = randArrayElement(surnames),
                            login = "teach${teacherList.size + 1}",
                            password = "1",
                        )
                    teacherList += teacher

                }
                else -> {
                    Log.e(TAG, "Wrong user group")
                }
            }
            val day = if (i + 1 < 10) "0${i + 1}" else "${i + 1}"
            createScheduleDay("${day}.11")
        }
        if (studentList.isEmpty()) Log.w(TAG, "Student list is empty")
        if (teacherList.isEmpty()) Log.w(TAG, "Teacher list is empty")
        if (scheduleList.isEmpty()) Log.w(TAG, "Schedule list is empty")
    }
}
