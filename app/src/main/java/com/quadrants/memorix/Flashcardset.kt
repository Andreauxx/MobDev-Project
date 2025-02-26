package com.quadrants.memorix

// Define Flashcard Data Class
data class Flashcard(
    val type: String = "term-definition", // Can be "term-definition" or "multiple-choice"
    val term: String = "",
    val definition: String = "",
    val explanation: String = "",
    val answers: List<String>? = null, // Only for multiple-choice
    val correctAnswerIndex: Int? = null, // Only for multiple-choice
    val streakCount: Int = 0, // Optional streak count,
    val createdBy: String = "",
) {

}

// Define FlashcardSet Data Class
data class FlashcardSet(
    val title: String = "",
    val category: String = "",
    val isPublic: Boolean = true,
    val createdBy: String = "",
    val cards: List<Flashcard> = emptyList(),
)
