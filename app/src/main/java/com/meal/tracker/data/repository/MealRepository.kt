package com.meal.tracker.data.repository

import com.meal.tracker.data.dao.MealRecordDao
import com.meal.tracker.data.dao.StudentDao
import com.meal.tracker.data.entity.MealRecordEntity
import com.meal.tracker.data.entity.StudentEntity
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

    /**
     * 一次性加载 [startDate, endDate] 区间（含）内的全部学生与打卡记录。
     * 用于导出 / 报表等需要把区间内数据打包成单次 IO 的场景。
     */
    suspend fun loadStudentsAndRecordsInRange(
        startDate: String,
        endDate: String
    ): Pair<List<StudentEntity>, List<MealRecordEntity>> {
        val students = getAllStudentsList()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val startCal = java.util.Calendar.getInstance().apply { time = dateFormat.parse(startDate)!! }
        val endCal = java.util.Calendar.getInstance().apply { time = dateFormat.parse(endDate)!! }

        val records = mutableListOf<MealRecordEntity>()
        while (!startCal.after(endCal)) {
            val dateStr = dateFormat.format(startCal.time)
            records += getRecordsByDateList(dateStr)
            startCal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return students to records
    }
}
