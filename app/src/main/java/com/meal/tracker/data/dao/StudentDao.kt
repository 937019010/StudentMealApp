package com.meal.tracker.data.dao

import androidx.room.*
import com.meal.tracker.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY orderIndex ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>
    
    @Query("SELECT * FROM students ORDER BY orderIndex ASC")
    suspend fun getAllStudentsList(): List<StudentEntity>
    
    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): StudentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)
    
    @Delete
    suspend fun deleteStudent(student: StudentEntity)
    
    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)
    
    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
    
    @Query("SELECT MAX(orderIndex) FROM students")
    suspend fun getMaxOrderIndex(): Int?
}
