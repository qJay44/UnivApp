package edu.muiv.univapp.utils

class TwoStringListsDifference {
    lateinit var newList: List<String>
    lateinit var oldList: List<String>

    fun compareLists(): Map<String, List<String>> {
        val diffLists = mutableMapOf<String, List<String>>()
        val deleteList = mutableListOf<String>()
        val upsertList = mutableListOf<String>()

        for (oldId in oldList)
            if (oldId !in newList)
                deleteList += oldId

        for (newId in newList)
            if (newId !in oldList)
                upsertList += newId

        diffLists["delete"] = deleteList
        diffLists["upsert"] = upsertList

        newList = emptyList()
        oldList = emptyList()

        return diffLists
    }
}
