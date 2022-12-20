package edu.muiv.univapp.ui.login.utils

import android.util.Log
import edu.muiv.univapp.ui.navigation.notifications.Notification
import edu.muiv.univapp.ui.navigation.profile.ProfileAttendance
import edu.muiv.univapp.ui.navigation.schedule.model.Schedule
import edu.muiv.univapp.model.Student
import edu.muiv.univapp.model.Subject
import edu.muiv.univapp.model.Teacher
import java.text.SimpleDateFormat
import java.util.*

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

    @Suppress("SpellCheckingInspection")
    private val patronymics: Array<String> = arrayOf(
        "Aalbers",
        "Aantjes",
        "Aarons (surname)",
        "Aaronson",
        "Aarts",
        "Aartsen",
        "Abbasov",
        "Abdulayev",
        "Abdullahi",
        "Abdullayev",
        "Abrahami",
        "Abrahamowicz",
        "Abrahams",
        "Abrahamsen",
        "Abrahamson",
        "Abrahamsson",
        "Abrahamyan",
        "Abramashvili",
        "Abramavičius",
        "Abramczyk",
        "Abramenko",
        "Abramishvili",
        "Abramović",
        "Abramowicz"
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

    private val scheduleTypes: Array<String> = arrayOf(
        "Лекция", "СПЗ"
    )

    @Suppress("SpellCheckingInspection")
    private const val SENTENCE =
        "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque " +
        "laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi " +
        "architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas " +
        "sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione " +
        "voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit " +
        "amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut " +
        "labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis " +
        "nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea " +
        "commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit" +
        "esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"

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

    private val examTypes: Array<String> = arrayOf(
        "Зачет", "Экзамен"
    )

    private val usedGroups = mutableListOf<String>()

    val studentList: MutableList<Student> = mutableListOf()
    val teacherList: MutableList<Teacher> = mutableListOf()
    val scheduleList: MutableList<Schedule> = mutableListOf()
    val notificationList: MutableList<Notification> = mutableListOf()
    val profileAttendanceList: MutableList<ProfileAttendance> = mutableListOf()
    val subject1List: MutableList<Subject> = mutableListOf()
    val subject2List: MutableList<Subject> = mutableListOf()

    private fun randInt(a: Int, b: Int) = (a..b).shuffled().last()
    private fun randArrayElement(arr: Array<String>) = arr[(arr.indices).shuffled().last()]

    // Weeks to create: previous, current and next
    private fun createScheduleForThreeWeeks(currentGroupName: String) {
        val format = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru"))
        val calendar = Calendar.getInstance(Locale.forLanguageTag("ru"))
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Set current week to previous
        calendar.add(Calendar.WEEK_OF_MONTH, -1)
        var roomCounter = 0
        for (k in 0 until 3) {
            val studyDays = randInt(1, 7)
            for (i in 0 until studyDays) {
                val amount = randInt(1, 6)
                val maxStartIndex = timeStart.size - amount
                val startIndex = randInt(0, maxStartIndex)
                val currentSubjectList =
                    if (currentGroupName.startsWith("ИД")) subject1List else subject2List

                for (j in 0 until amount) {
                    val schedule = Schedule(
                        id = UUID.randomUUID(),
                        date = format.format(calendar.time),
                        timeStart = timeStart[startIndex + j],
                        timeEnd = timeEnd[startIndex + j],
                        roomNum = ++roomCounter,
                        type = randArrayElement(scheduleTypes),
                        subjectID = currentSubjectList[(currentSubjectList.indices).shuffled().last()].id,
                        teacherID = teacherList[(teacherList.indices).shuffled().last()].id
                    )

                    scheduleList += schedule
                }
                // Next day
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            // Subtract extra day
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            // Set day of week to Monday
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            // Next week
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        }
        usedGroups += currentGroupName
    }

    private fun createNotificationsForTwoMonths() {
        val format = SimpleDateFormat("dd.MM", Locale.forLanguageTag("ru"))
        val calendar = Calendar.getInstance(Locale.forLanguageTag("ru"))
        val currentDay = calendar.time

        // Set month to previous
        calendar.add(Calendar.MONTH, -1)
        var i = 0
        while (calendar.time != currentDay) {
            val notification = Notification(
                UUID.randomUUID(),
                format.format(calendar.time),
                "Уведомление №$i",
                ""
            )
            val words = SENTENCE.split(" ")
            val length = (1..words.size).shuffled().last()

            for (k in 0 until length) {
                notification.text += words.shuffled().last() + " "
            }
            notificationList += notification

            // Next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            i++
        }
        notificationList.reverse()
    }

    private fun createProfileAttendance() {
        val boolList = listOf(false, true)
        for (student in studentList) {
            for (schedule in scheduleList.withIndex()) {
                for (pair in subject1List zip subject2List) {
                    if (student.groupName == pair.first.groupName
                        || student.groupName == pair.second.groupName) {

                        val profileAttendance = ProfileAttendance(
                            id = UUID.randomUUID(),
                            scheduleID = schedule.value.id,
                            userID = student.id,
                            visited = boolList.shuffled().last()
                        )
                        profileAttendanceList += profileAttendance
                    }
                }
                // Cut search amount
                if (schedule.index >= 30) break
            }
        }
    }

    private fun createSubjects() {
        val unusedTeachers = mutableListOf<Teacher>()
        unusedTeachers.addAll(teacherList)
        groupNames1.zip(groupNames2).forEach { (a, b) ->
            if (unusedTeachers.isEmpty()) return
            subjectNames1.zip(subjectNames2).forEach { (x, y) ->
                if (unusedTeachers.isEmpty()) return
                var teacher = unusedTeachers.shuffled().last()
                var examTypeIndex = randInt(0, 1)

                // For IT faculty
                val subject1 = Subject(
                    id = UUID.randomUUID(),
                    subjectName = x,
                    groupName = a,
                    teacherID = teacher.id,
                    examType = examTypes[examTypeIndex]
                )
                subject1List += subject1
                unusedTeachers.removeLast()

                if (unusedTeachers.isEmpty()) return
                teacher = unusedTeachers.shuffled().last()

                examTypeIndex = randInt(0, 1)
                // For Economics faculty
                val subject2 = Subject(
                    id = UUID.randomUUID(),
                    subjectName = y,
                    groupName = b,
                    teacherID = teacher.id,
                    examType = examTypes[examTypeIndex]
                )
                subject2List += subject2
                unusedTeachers.removeLast()
            }
        }
    }

    fun createAll(amount: Int) {
        for (i in 0 until amount) {
            val isHalf = i > amount / 2 - 1

            when (if (isHalf) userGroups[0] else userGroups[1]) {
                "Student" -> {
                    val currentGroupName =
                        if (i % 2 == 0) randArrayElement(groupNames1)
                        else randArrayElement(groupNames2)

                    val currCourse = randInt(1, 4)
                    val currSemester = currCourse * randInt(1, 2)
                    val student =
                        Student(
                            id = UUID.randomUUID(),
                            name = randArrayElement(names),
                            surname = randArrayElement(surnames),
                            patronymic = randArrayElement(patronymics),
                            login = "stud${studentList.size + 1}",
                            password = "1",
                            groupName = currentGroupName,
                            course = currCourse.toString(),
                            semester = currSemester.toString()
                        )
                    studentList += student
                }
                "Teacher" -> {
                    val teacher =
                        Teacher(
                            id = UUID.randomUUID(),
                            name = randArrayElement(names),
                            surname = randArrayElement(surnames),
                            patronymic = randArrayElement(patronymics),
                            login = "teach${teacherList.size + 1}",
                            password = "1",
                        )
                    teacherList += teacher

                }
                else -> {
                    Log.e(TAG, "Wrong user group")
                }
            }
        }
        createSubjects()

        for (group in groupNames1) {
            createScheduleForThreeWeeks(group)
        }
        for (group in groupNames2) {
            createScheduleForThreeWeeks(group)
        }

        createNotificationsForTwoMonths()
        createProfileAttendance()
    }
}
