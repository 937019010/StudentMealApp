package com.meal.tracker.domain.model

data class Student(
    val id: Long = 0,
    val name: String,
    val orderIndex: Int = 0
)

data class MealRecord(
    val studentId: Long,
    val date: String,
    val mealType: MealType,
    val isMarked: Boolean = false
)

enum class MealType(val displayName: String, val value: String) {
    BREAKFAST("早餐", "breakfast"),
    LUNCH("午餐", "lunch"),
    DINNER("晚餐", "dinner");
    
    companion object {
        fun fromValue(value: String): MealType {
            return entries.find { it.value == value } ?: BREAKFAST
        }
    }
}

data class StudentWithMeals(
    val student: Student,
    val breakfastMarked: Boolean = false,
    val lunchMarked: Boolean = false,
    val dinnerMarked: Boolean = false
)

data class DailyStatistics(
    val date: String,
    val totalStudents: Int,
    val markedCount: Int,
    val unmarkedCount: Int,
    val breakfastMarked: Int,
    val lunchMarked: Int,
    val dinnerMarked: Int
)

data class StudentStatistics(
    val student: Student,
    val totalMarked: Int,
    val breakfastMarked: Int,
    val lunchMarked: Int,
    val dinnerMarked: Int
)

enum class MealFilter {
    ALL, BREAKFAST, LUNCH, DINNER
}

sealed interface ExportResult {
    data class Success(val path: String) : ExportResult
    data class Error(val message: String) : ExportResult
}