package com.meal.tracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.meal.tracker.data.dao.MealRecordDao
import com.meal.tracker.data.dao.StudentDao
import com.meal.tracker.data.entity.MealRecordEntity
import com.meal.tracker.data.entity.StudentEntity

@Database(
    entities = [StudentEntity::class, MealRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun mealRecordDao(): MealRecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_meal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
