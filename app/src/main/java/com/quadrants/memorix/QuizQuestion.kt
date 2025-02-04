package com.quadrants.memorix

data class QuizQuestion(
    val question: String = "",
    val answers: List<String> = emptyList(), // ✅ Fixed: Default value to avoid crashes
    val correctAnswer: String = ""
)
