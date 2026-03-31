package com.example.offlineplayer.search

object T9Search {
    private val mapping = mapOf('2' to "abc", '3' to "def", '4' to "ghi", '5' to "jkl", '6' to "mno", '7' to "pqrs", '8' to "tuv", '9' to "wxyz")
    fun match(query: String, text: String): Boolean {
        if (query.isEmpty()) return true
        val words = text.lowercase().split(" ")
        for (word in words) {
            if (word.length < query.length) continue
            var match = true
            for (i in query.indices) {
                val possibleChars = mapping[query[i]] ?: query[i].toString()
                if (word[i] !in possibleChars) { match = false; break }
            }
            if (match) return true
        }
        return false
    }
}


