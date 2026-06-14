package com.mealapp.data.repository

import com.mealapp.data.dao.MealRecordDao
import com.mealapp.data.dao.StudentDao
import com.mealapp.data.entity.MealRecordEntity
import com.mealapp.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

class MealRepository(
    private val studentDao: StudentDao,
    private val mealRecordDao: MealRecordDao
) {
    // Student operations
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()
    
    suspend fun getAllStudentsList(): List<StudentEntity> = studentDao.getAllStudentsList()
    
    suspend fun getStudentById(id: Long): StudentEntity? = studentDao.getStudentById(id)
    
    suspend fun insertStudent(student: StudentEntity): Long = studentDao.insertStudent(student)
    
    suspend fun insertStudents(students: List<StudentEntity>) = studentDao.insertStudents(students)
    
    suspend fun updateStudent(student: StudentEntity) = studentDao.updateStudent(student)
    
    suspend fun deleteStudent(student: StudentEntity) = studentDao.deleteStudent(student)
    
    suspend fun deleteStudentById(id: Long) = studentDao.deleteStudentById(id)
    
    suspend fun getStudentCount(): Int = studentDao.getStudentCount()
    
    suspend fun getMaxOrderIndex(): Int = studentDao.getMaxOrderIndex() ?: 0
    
    // Meal record operations
    fun getRecordsByDate(date: String): Flow<List<MealRecordEntity>> = 
        mealRecordDao.getRecordsByDate(date)
    
    suspend fun getRecordsByDateList(date: String): List<MealRecordEntity> = 
        mealRecordDao.getRecordsByDateList(date)
    
    fun getRecordsByStudent(studentId: Long): Flow<List<MealRecordEntity>> =
        mealRecordDao.getRecordsByStudent(studentId)
    
    fun getRecordsByStudentAndDateRange(studentId: Long, startDate: String, endDate: String): Flow<List<MealRecordEntity>> =
        mealRecordDao.getRecordsByStudentAndDateRange(studentId, startDate, endDate)
    
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<MealRecordEntity>> =
        mealRecordDao.getRecordsByDateRange(startDate, endDate)
    
    suspend fun getRecord(studentId: Long, date: String, mealType: String): MealRecordEntity? =
        mealRecordDao.getRecord(studentId, date, mealType)
    
    suspend fun insertRecord(record: MealRecordEntity) = mealRecordDao.insertRecord(record)
    
    suspend fun insertRecords(records: List<MealRecordEntity>) = mealRecordDao.insertRecords(records)
    
    suspend fun updateRecord(record: MealRecordEntity) = mealRecordDao.updateRecord(record)
    
    suspend fun deleteRecordsByDate(date: String) = mealRecordDao.deleteRecordsByDate(date)
    
    suspend fun deleteRecordsByStudent(studentId: Long) = mealRecordDao.deleteRecordsByStudent(studentId)
    
    suspend fun getMarkedCountByDate(date: String): Int = mealRecordDao.getMarkedCountByDate(date)
    
    suspend fun getTotalCountByDate(date: String): Int = mealRecordDao.getTotalCountByDate(date)
}
