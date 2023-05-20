package edu.muiv.univapp.utils

class TwoStringListsDifference {
    lateinit var newList: List<String>
    lateinit var oldList: List<String>
    val deleteList by lazy { compareLists() }

    private fun compareLists(): List<String> {
        val deleteList = mutableListOf<String>()
        for (oldId in oldList)
            if (oldId !in newList)
                deleteList += oldId

        return deleteList.toList()
    }
}
