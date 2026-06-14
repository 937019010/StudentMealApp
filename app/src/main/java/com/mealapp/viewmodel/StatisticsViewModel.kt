package com.mealapp.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mealapp.data.database.AppDatabase
import com.mealapp.data.entity.MealRecordEntity
import com.mealapp.data.repository.MealRepository
import com.mealapp.domain.model.*
import com.mealapp.domain.usecase.StatisticsUseCase
import com.opencsv.CSVWriter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MealRepository(database.studentDao(), database.mealRecordDao())
    private val statisticsUseCase = StatisticsUseCase(repository)
    
    private val _startDate = MutableStateFlow(getDateDaysAgo(30))
    val startDate: StateFlow<String> = _startDate.asStateFlow()
    
    private val _endDate = MutableStateFlow(getCurrentDate())
    val endDate: StateFlow<String> = _endDate.asStateFlow()
    
    val studentStatistics: StateFlow<List<StudentStatistics>> = combine(
        _startDate,
        _endDate
    ) { start, end ->
        Pair(start, end)
    }.flatMapLatest { (start, end) ->
        statisticsUseCase.getAllStudentsStatistics(start, end)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult.asSharedFlow()
    
    fun setDateRange(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
    }
    
    fun exportToCSV(context: Context) {
        viewModelScope.launch {
            try {
                val students = repository.getAllStudentsList()
                val records = mutableListOf<MealRecordEntity>()
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startCal = Calendar.getInstance().apply { time = dateFormat.parse(_startDate.value)!! }
                val endCal = Calendar.getInstance().apply { time = dateFormat.parse(_endDate.value)!! }
                
                while (!startCal.after(endCal)) {
                    val dateStr = dateFormat.format(startCal.time)
                    records.addAll(repository.getRecordsByDateList(dateStr))
                    startCal.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                val exportDir = getExportDirectory(context)
                val fileName = "就餐统计_${_startDate.value}_${_endDate.value}.csv"
                val file = File(exportDir, fileName)
                
                CSVWriter(FileWriter(file)).use { writer ->
                    // Header
                    writer.writeNext(arrayOf("姓名", "日期", "早餐", "午餐", "晚餐"))
                    
                    val dateRecords = records.groupBy { it.date }
                    dateRecords.forEach { (date, dayRecords) ->
                        students.forEach { student ->
                            val breakfast = dayRecords.find { it.studentId == student.id && it.mealType == "breakfast" }?.isMarked ?: false
                            val lunch = dayRecords.find { it.studentId == student.id && it.mealType == "lunch" }?.isMarked ?: false
                            val dinner = dayRecords.find { it.studentId == student.id && it.mealType == "dinner" }?.isMarked ?: false
                            writer.writeNext(arrayOf(
                                student.name, date,
                                if (breakfast) "✓" else "",
                                if (lunch) "✓" else "",
                                if (dinner) "✓" else ""
                            ))
                        }
                    }
                }
                
                _exportResult.emit(ExportResult.Success(file.absolutePath))
            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Error(e.message ?: "导出失败"))
            }
        }
    }
    
    fun exportToExcel(context: Context) {
        viewModelScope.launch {
            try {
                val students = repository.getAllStudentsList()
                val records = mutableListOf<MealRecordEntity>()
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startCal = Calendar.getInstance().apply { time = dateFormat.parse(_startDate.value)!! }
                val endCal = Calendar.getInstance().apply { time = dateFormat.parse(_endDate.value)!! }
                
                while (!startCal.after(endCal)) {
                    val dateStr = dateFormat.format(startCal.time)
                    records.addAll(repository.getRecordsByDateList(dateStr))
                    startCal.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("就餐统计")
                
                // Header
                val headerRow = sheet.createRow(0)
                headerRow.createCell(0).setCellValue("姓名")
                headerRow.createCell(1).setCellValue("早餐")
                headerRow.createCell(2).setCellValue("午餐")
                headerRow.createCell(3).setCellValue("晚餐")
                headerRow.createCell(4).setCellValue("总计")
                
                // Data
                students.forEachIndexed { index, student ->
                    val studentRecords = records.filter { it.studentId == student.id }
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(student.name)
                    row.createCell(1).setCellValue(studentRecords.count { it.mealType == "breakfast" && it.isMarked }.toString())
                    row.createCell(2).setCellValue(studentRecords.count { it.mealType == "lunch" && it.isMarked }.toString())
                    row.createCell(3).setCellValue(studentRecords.count { it.mealType == "dinner" && it.isMarked }.toString())
                    row.createCell(4).setCellValue(studentRecords.count { it.isMarked }.toString())
                }
                
                val exportDir = getExportDirectory(context)
                val fileName = "就餐统计_${_startDate.value}_${_endDate.value}.xlsx"
                val file = File(exportDir, fileName)
                
                FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                }
                workbook.close()
                
                _exportResult.emit(ExportResult.Success(file.absolutePath))
            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Error(e.message ?: "导出失败"))
            }
        }
    }
    
    private fun getExportDirectory(context: Context): File {
        // Read user's custom export path from settings
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val customPath = prefs.getString("export_path", null)
        
        return if (customPath != null) {
            val dir = File(customPath)
            if (!dir.exists()) dir.mkdirs()
            dir
        } else {
            val dir = File(context.getExternalFilesDir(null), "exports")
            if (!dir.exists()) dir.mkdirs()
            dir
        }
    }
    
    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    
    private fun getDateDaysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}

sealed class ExportResult {
    data class Success(val path: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
