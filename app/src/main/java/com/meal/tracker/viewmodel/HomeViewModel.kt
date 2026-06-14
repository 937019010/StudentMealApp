package com.meal.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meal.tracker.di.ServiceLocator
import com.meal.tracker.domain.model.*
import com.meal.tracker.domain.usecase.GetDailyStudentsWithMealsUseCase
import com.meal.tracker.domain.usecase.ToggleMealMarkUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.mealRepository
    private val exportRepository = ServiceLocator.exportRepository
    private val getDailyStudentsUseCase = GetDailyStudentsWithMealsUseCase(repository)
    private val toggleMealMarkUseCase = ToggleMealMarkUseCase(repository)

    private val _selectedDate = MutableStateFlow(today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _mealFilter = MutableStateFlow(MealFilter.ALL)
    val mealFilter: StateFlow<MealFilter> = _mealFilter.asStateFlow()

    val studentsWithMeals: StateFlow<List<StudentWithMeals>> = _selectedDate
        .flatMapLatest { date -> getDailyStudentsUseCase(date) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * 过滤 + 排序后的列表：学生按 [StudentSort] 规则在当前 [mealFilter] 下重排。
     * 切分类时即时刷新，不需要重新查 DB。
     */
    val filteredStudents: StateFlow<List<StudentWithMeals>> =
        combine(studentsWithMeals, _mealFilter) { list, filter ->
            StudentSort.sort(list, filter)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val statistics: StateFlow<DailyStatistics?> = studentsWithMeals
        .map { list ->
            DailyStatistics(
                date = _selectedDate.value,
                totalStudents = list.size,
                markedCount = list.count { it.breakfastMarked || it.lunchMarked || it.dinnerMarked },
                unmarkedCount = list.size * 3 - list.sumOf { countOf(it) },
                breakfastMarked = list.count { it.breakfastMarked },
                lunchMarked = list.count { it.lunchMarked },
                dinnerMarked = list.count { it.dinnerMarked }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult.asSharedFlow()

    fun selectDate(date: String) { _selectedDate.value = date }
    fun goToPreviousDay() = shiftDate(-1)
    fun goToNextDay() = shiftDate(+1)
    fun setMealFilter(filter: MealFilter) { _mealFilter.value = filter }

    fun toggleMealMark(studentId: Long, mealType: MealType) {
        viewModelScope.launch {
            toggleMealMarkUseCase(studentId, _selectedDate.value, mealType)
        }
    }

    /** "保存"按钮：把当前选中的日期导出为 CSV。 */
    fun exportToday() {
        val date = _selectedDate.value
        viewModelScope.launch {
            runCatching { exportRepository.exportCsv(date, date) }
                .onSuccess { file -> _exportResult.emit(ExportResult.Success(file.absolutePath)) }
                .onFailure { e -> _exportResult.emit(ExportResult.Error(e.message ?: "保存失败")) }
        }
    }

    private fun countOf(item: StudentWithMeals): Int =
        listOf(item.breakfastMarked, item.lunchMarked, item.dinnerMarked).count { it }

    private fun shiftDate(deltaDays: Int) {
        val cal = Calendar.getInstance().apply {
            time = DATE_FORMAT.parse(_selectedDate.value) ?: Date()
            add(Calendar.DAY_OF_MONTH, deltaDays)
        }
        _selectedDate.value = DATE_FORMAT.format(cal.time)
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun today(): String = DATE_FORMAT.format(Date())

        fun formatDisplayDate(dateStr: String): String = try {
            val display = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            display.format(DATE_FORMAT.parse(dateStr)!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}
