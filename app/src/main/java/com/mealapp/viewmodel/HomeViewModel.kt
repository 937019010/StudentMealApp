package com.mealapp.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mealapp.data.database.AppDatabase
import com.mealapp.data.repository.MealRepository
import com.mealapp.domain.model.*
import com.mealapp.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MealRepository(database.studentDao(), database.mealRecordDao())
    
    private val getDailyStudentsUseCase = GetDailyStudentsWithMealsUseCase(repository)
    private val toggleMealMarkUseCase = ToggleMealMarkUseCase(repository)
    
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _mealFilter = MutableStateFlow(MealFilter.ALL)
    val mealFilter: StateFlow<MealFilter> = _mealFilter.asStateFlow()
    
    val studentsWithMeals: StateFlow<List<StudentWithMeals>> = _selectedDate
        .flatMapLatest { date -> getDailyStudentsUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val filteredStudents: StateFlow<List<StudentWithMeals>> = studentsWithMeals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val statistics: StateFlow<DailyStatistics?> = _selectedDate
        .map { date ->
            val students = studentsWithMeals.value
            DailyStatistics(
                date = date,
                totalStudents = students.size,
                markedCount = students.count { it.breakfastMarked || it.lunchMarked || it.dinnerMarked },
                unmarkedCount = students.size * 3 - (students.count { it.breakfastMarked } + students.count { it.lunchMarked } + students.count { it.dinnerMarked }),
                breakfastMarked = students.count { it.breakfastMarked },
                lunchMarked = students.count { it.lunchMarked },
                dinnerMarked = students.count { it.dinnerMarked }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    fun selectDate(date: String) {
        _selectedDate.value = date
    }
    
    fun goToPreviousDay() {
        val date = parseDate(_selectedDate.value)
        date.add(Calendar.DAY_OF_MONTH, -1)
        _selectedDate.value = formatDate(date)
    }
    
    fun goToNextDay() {
        val date = parseDate(_selectedDate.value)
        date.add(Calendar.DAY_OF_MONTH, 1)
        _selectedDate.value = formatDate(date)
    }
    
    fun setMealFilter(filter: MealFilter) {
        _mealFilter.value = filter
    }
    
    fun toggleMealMark(studentId: Long, mealType: MealType) {
        viewModelScope.launch {
            toggleMealMarkUseCase(studentId, _selectedDate.value, mealType)
        }
    }
    
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    private fun parseDate(dateStr: String): Calendar {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return Calendar.getInstance().apply { time = sdf.parse(dateStr)!! }
    }
    
    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
    
    companion object {
        fun formatDisplayDate(dateStr: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displaySdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                val date = sdf.parse(dateStr)
                displaySdf.format(date!!)
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}
