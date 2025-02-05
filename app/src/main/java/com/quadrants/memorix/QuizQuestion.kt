package com.quadrants.memorix

data class QuizQuestion(
    val question: String = "",
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0 // ✅ Fix: Use an Int instead of a String
)
