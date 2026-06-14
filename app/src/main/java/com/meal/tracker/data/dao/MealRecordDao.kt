package com.meal.tracker.data.dao

import androidx.room.*
import com.meal.tracker.data.entity.MealRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealRecordDao {
    @Query("SELECT * FROM meal_records WHERE date = :date")
    fun getRecordsByDate(date: String): Flow<List<MealRecordEntity>>
    
    @Query("SELECT * FROM meal_records WHERE date = :date")
    suspend fun getRecordsByDateList(date: String): List<MealRecordEntity>
    
    @Query("SELECT * FROM meal_records WHERE studentId = :studentId")
    fun getRecordsByStudent(studentId: Long): Flow<List<MealRecordEntity>>
    
    @Query("SELECT * FROM meal_records WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate")
    fun getRecordsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<MealRecordEntity>>
    
    @Query("SELECT * FROM meal_records WHERE date BETWEEN :startDate AND :endDate")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<MealRecordEntity>>
    
    @Query("SELECT * FROM meal_records WHERE studentId = :studentId AND date = :date AND mealType = :mealType")
    suspend fun getRecord(studentId: Long, date: String, mealType: String): MealRecordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MealRecordEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<MealRecordEntity>)
    
    @Update
    suspend fun updateRecord(record: MealRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: MealRecordEntity)
    
    @Query("DELETE FROM meal_records WHERE date = :date")
    suspend fun deleteRecordsByDate(date: String)
    
    @Query("DELETE FROM meal_records WHERE studentId = :studentId")
    suspend fun deleteRecordsByStudent(studentId: Long)
    
    @Query("SELECT COUNT(*) FROM meal_records WHERE date = :date AND isMarked = 1")
    suspend fun getMarkedCountByDate(date: String): Int
    
    @Query("SELECT COUNT(*) FROM meal_records WHERE date = :date")
    suspend fun getTotalCountByDate(date: String): Int
}
