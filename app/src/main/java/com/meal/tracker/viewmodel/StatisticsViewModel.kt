package com.meal.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meal.tracker.di.ServiceLocator
import com.meal.tracker.data.export.ExportRepository
import com.meal.tracker.domain.model.ExportResult
import com.meal.tracker.domain.model.StudentStatistics
import com.meal.tracker.domain.usecase.StatisticsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.mealRepository
    private val statisticsUseCase = StatisticsUseCase(repository)
    private val exportRepository: ExportRepository = ServiceLocator.exportRepository

    private val _startDate = MutableStateFlow(daysAgo(30))
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(today())
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    val studentStatistics: StateFlow<List<StudentStatistics>> = combine(_startDate, _endDate) { s, e -> s to e }
        .flatMapLatest { (s, e) -> statisticsUseCase.getAllStudentsStatistics(s, e) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult.asSharedFlow()

    fun setDateRange(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
    }

    fun exportToCSV() = launchExport { exportRepository.exportCsv(_startDate.value, _endDate.value) }
    fun exportToExcel() = launchExport { exportRepository.exportExcel(_startDate.value, _endDate.value) }

    private fun launchExport(block: suspend () -> java.io.File) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { file -> _exportResult.emit(ExportResult.Success(file.absolutePath)) }
                .onFailure { e -> _exportResult.emit(ExportResult.Error(e.message ?: "导出失败")) }
        }
    }

    private fun daysAgo(days: Int): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -days) }
        return DATE_FORMAT.format(cal.time)
    }

    private fun today(): String = DATE_FORMAT.format(Date())

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}