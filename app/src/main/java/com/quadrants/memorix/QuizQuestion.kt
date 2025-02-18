package com.quadrants.memorix

data class QuizQuestion(
    val question: String = "",
    val answers: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "", // ✅ Optional explanation
    val timeLimit: Int = 30, // ✅ Added time limit
    val quizIds: List<String> = emptyList(), // ✅ Supports multiple quizzes
    val subjectIds: List<String> = emptyList(), // ✅ Supports multiple subjects
    val flashcardSetIds: List<String> = emptyList(), // ✅ Supports flashcard sets
    val createdBy: String? = null, // ✅ If null, the question is public
    val sharedWith: List<String> = emptyList(), // ✅ List of user IDs with access
    val createdAt: Long = System.currentTimeMillis(), // ✅ Timestamp for when question was created
    val difficulty: String = "Medium", // ✅ Easy, Medium, Hard
    val questionType: String = "multiple_choice" // ✅ Question type
)
