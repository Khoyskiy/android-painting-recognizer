package com.example.art.analyzer

class MatchTracker(private val requiredConfirmations: Int = 3) {

    private val recentMatches = ArrayDeque<String>()

    fun track(name: String?): Boolean {
        if (name.isNullOrEmpty()) return false

        if (recentMatches.size == requiredConfirmations) {
            recentMatches.removeFirst()
        }
        recentMatches.addLast(name)

        return recentMatches.size == requiredConfirmations && recentMatches.all { it == name }
    }

    fun reset() {
        recentMatches.clear()
    }
}
