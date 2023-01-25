package edu.muiv.univapp.utils

class CompareLists <T> {
    lateinit var newList: List<T>
    lateinit var oldList: List<T>
    val deleteList by lazy { findIdsToDelete() }

    private fun findIdsToDelete(): List<T> {
        val deleteList = mutableListOf<T>()
        for (oldId in oldList)
            if (oldId !in newList)
                deleteList += oldId

        return deleteList.toList()
    }

    inline fun createList(list: List<T>, type: Int, doAfter: () -> Unit) {
        when (type) {
            FetchedListType.NEW.type -> {
                newList = list
                doAfter.invoke()
            }
            FetchedListType.OLD.type -> {
                oldList = list
                doAfter.invoke()
            }
        }
    }
}