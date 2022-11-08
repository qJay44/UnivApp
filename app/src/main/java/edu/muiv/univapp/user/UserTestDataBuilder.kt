package edu.muiv.univapp.user

object UserTestDataBuilder {

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

    private fun randArrayChoice(arr: Array<String>) = arr[(arr.indices).shuffled().last()]

    fun createUser(amount: Int): List<User> {
        val userList: MutableList<User> = mutableListOf()

        for (i in 0 until amount) {
            val groupNames = if ((0..1).shuffled().last() == 0) groupNames1 else groupNames2
            val choseUserGroup = randArrayChoice(userGroups)
            val choseStudentGroup = if (choseUserGroup == "Student") randArrayChoice(groupNames) else null
            val user = User(
                login = "test${i}",
                password = "1",
                name = randArrayChoice(names),
                surname = randArrayChoice(surnames),
                userGroup = choseUserGroup,
                studentGroup = choseStudentGroup
            )

            userList += user
        }

        return userList
    }
}