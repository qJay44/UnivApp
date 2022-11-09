package edu.muiv.univapp.user

import android.util.Log
import edu.muiv.univapp.schedule.Schedule
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

    val userList: MutableList<User> = mutableListOf()
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
                date = scheduleDate,
                timeStart = timeStart[startIndex + i],
                timeEnd = timeEnd[startIndex + i],
                subjectName = faculty[(faculty.indices).shuffled().last()],
                roomNum = randInt(100, 525),
                studentGroup = groupName[(groupName.indices).shuffled().last()]
            )

            scheduleList += schedule
        }
    }

    private fun randArrayElement(arr: Array<String>) = arr[(arr.indices).shuffled().last()]

    fun createAll(amount: Int) {

        for (i in 0 until amount) {
            val isFirstHalf = i < amount / 2
            val choseUserGroup = if (isFirstHalf) userGroups[0] else userGroups[1]
            val id = UUID.randomUUID()
            val user = User(
                id = id,
                login = "test${i}",
                password = "1",
                userType = choseUserGroup
            )
            userList += user
            if (userList.size == 0) Log.w(TAG, "User list is empty")

            when (choseUserGroup) {
                "Student" -> {
                    val student =
                        Student(
                            name = randArrayElement(names),
                            surname = randArrayElement(surnames),
                            groupName = if (isFirstHalf) groupNames1[i] else groupNames2[i],
                            userID = id
                        )
                    studentList += student
                    if (studentList.size == 0) Log.w(TAG, "Student list is empty")
                }
                "Teacher" -> {
                    createScheduleDay("0${i}.11")
                    for (schedule in scheduleList) {
                        val teacher =
                            Teacher(
                                name = randArrayElement(names),
                                surname = randArrayElement(surnames),
                                scheduleID = schedule.id,
                                userID = id
                            )
                        teacherList += teacher

                        if (teacherList.size == 0) Log.w(TAG, "Teacher list is empty")
                    }
                }
                else -> {
                    throw IllegalStateException("Wrong user group")
                }
            }
        }
    }
}
