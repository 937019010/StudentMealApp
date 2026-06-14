package com.mealapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "meal_records",
    primaryKeys = ["studentId", "date", "mealType"],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("date")]
)
data class MealRecordEntity(
    val studentId: Long,
    val date: String, // Format: yyyy-MM-dd
    val mealType: String, // "breakfast", "lunch", "dinner"
    val isMarked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
