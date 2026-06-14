package com.mealapp.domain.usecase

import com.mealapp.data.entity.MealRecordEntity
import com.mealapp.data.entity.StudentEntity
import com.mealapp.data.repository.MealRepository
import com.mealapp.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class GetDailyStudentsWithMealsUseCase(private val repository: MealRepository) {
    operator fun invoke(date: String): Flow<List<StudentWithMeals>> {
        return combine(
            repository.allStudents,
            repository.getRecordsByDate(date)
        ) { students, records ->
            val recordMap = records.associateBy { "${it.studentId}_${it.mealType}" }
            val studentWithMealsList = students.map { student ->
                StudentWithMeals(
                    student = student.toModel(),
                    breakfastMarked = recordMap["${student.id}_breakfast"]?.isMarked ?: false,
                    lunchMarked = recordMap["${student.id}_lunch"]?.isMarked ?: false,
                    dinnerMarked = recordMap["${student.id}_dinner"]?.isMarked ?: false
                )
            }
            // Sort: unmarked students first by name, then marked students at bottom
            studentWithMealsList.sortedWith(
                compareBy<StudentWithMeals> { isAllMarked(it) }
                    .thenBy { it.student.name }
            )
        }
    }
    
    private fun isAllMarked(item: StudentWithMeals): Boolean {
        return item.breakfastMarked && item.lunchMarked && item.dinnerMarked
    }
}

class ToggleMealMarkUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(studentId: Long, date: String, mealType: MealType) {
        val existing = repository.getRecord(studentId, date, mealType.value)
        val newMarked = existing?.isMarked?.not() ?: true
        repository.insertRecord(
            MealRecordEntity(
                studentId = studentId,
                date = date,
                mealType = mealType.value,
                isMarked = newMarked,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}

class StudentManagementUseCase(private val repository: MealRepository) {
    suspend fun addStudent(name: String) {
        val maxIndex = repository.getMaxOrderIndex()
        repository.insertStudent(
            StudentEntity(name = name, orderIndex = maxIndex + 1)
        )
    }
    
    suspend fun addStudents(names: List<String>) {
        var maxIndex = repository.getMaxOrderIndex()
        val students = names.map { name ->
            maxIndex++
            StudentEntity(name = name, orderIndex = maxIndex)
        }
        repository.insertStudents(students)
    }
    
    suspend fun updateStudent(id: Long, newName: String) {
        repository.getStudentById(id)?.let { student ->
            repository.updateStudent(student.copy(name = newName))
        }
    }
    
    suspend fun deleteStudent(id: Long) {
        repository.deleteStudentById(id)
    }
    
    suspend fun reorderStudents(studentIds: List<Long>) {
        studentIds.forEachIndexed { index, id ->
            repository.getStudentById(id)?.let { student ->
                repository.updateStudent(student.copy(orderIndex = index))
            }
        }
    }
}

class StatisticsUseCase(private val repository: MealRepository) {
    fun getDailyStatistics(date: String, totalStudents: Int): Flow<DailyStatistics> {
        return repository.getRecordsByDate(date).map { records ->
            DailyStatistics(
                date = date,
                totalStudents = totalStudents,
                markedCount = records.count { it.isMarked },
                unmarkedCount = totalStudents * 3 - records.count { it.isMarked },
                breakfastMarked = records.count { it.mealType == "breakfast" && it.isMarked },
                lunchMarked = records.count { it.mealType == "lunch" && it.isMarked },
                dinnerMarked = records.count { it.mealType == "dinner" && it.isMarked }
            )
        }
    }
    
    fun getStudentStatistics(studentId: Long, startDate: String, endDate: String): Flow<StudentStatistics?> {
        return repository.getRecordsByStudentAndDateRange(studentId, startDate, endDate).map { records ->
            repository.getStudentById(studentId)?.let { student ->
                StudentStatistics(
                    student = student.toModel(),
                    totalMarked = records.count { it.isMarked },
                    breakfastMarked = records.count { it.mealType == "breakfast" && it.isMarked },
                    lunchMarked = records.count { it.mealType == "lunch" && it.isMarked },
                    dinnerMarked = records.count { it.mealType == "dinner" && it.isMarked }
                )
            }
        }
    }
    
    fun getAllStudentsStatistics(startDate: String, endDate: String) = 
        repository.getRecordsByDateRange(startDate, endDate).map { records ->
            val studentIds = records.map { it.studentId }.distinct()
            studentIds.mapNotNull { studentId ->
                val studentRecords = records.filter { it.studentId == studentId }
                repository.getStudentById(studentId)?.let { student ->
                    StudentStatistics(
                        student = student.toModel(),
                        totalMarked = studentRecords.count { it.isMarked },
                        breakfastMarked = studentRecords.count { it.mealType == "breakfast" && it.isMarked },
                        lunchMarked = studentRecords.count { it.mealType == "lunch" && it.isMarked },
                        dinnerMarked = studentRecords.count { it.mealType == "dinner" && it.isMarked }
                    )
                }
            }
        }
}

private fun StudentEntity.toModel() = Student(id = id, name = name, orderIndex = orderIndex)
