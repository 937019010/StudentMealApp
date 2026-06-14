package com.mealapp.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mealapp.data.database.AppDatabase
import com.mealapp.data.entity.StudentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(application)
    private val studentDao = database.studentDao()
    
    private val _exportPath = MutableStateFlow(getDefaultExportPath())
    val exportPath: StateFlow<String> = _exportPath.asStateFlow()
    
    private val _operationResult = MutableSharedFlow<SettingsResult>()
    val operationResult: SharedFlow<SettingsResult> = _operationResult.asSharedFlow()
    
    private fun getDefaultExportPath(): String {
        return File(context.getExternalFilesDir(null), "exports").absolutePath
    }
    
    fun setExportPath(path: String) {
        _exportPath.value = path
        // Save to SharedPreferences
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("export_path", path)
            .apply()
    }
    
    fun loadSettings() {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedPath = prefs.getString("export_path", getDefaultExportPath())
        _exportPath.value = savedPath ?: getDefaultExportPath()
    }
    
    fun backupData() {
        viewModelScope.launch {
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                
                val timestamp = System.currentTimeMillis()
                val backupFile = File(backupDir, "backup_$timestamp.json")
                
                // Export students to JSON
                val students = studentDao.getAllStudentsList()
                val jsonContent = buildString {
                    appendLine("[")
                    students.forEachIndexed { index, student ->
                        appendLine("  {")
                        appendLine("    \"id\": ${student.id},")
                        appendLine("    \"name\": \"${student.name}\",")
                        appendLine("    \"orderIndex\": ${student.orderIndex}")
                        append("  }")
                        if (index < students.size - 1) appendLine(",") else appendLine()
                    }
                    appendLine("]")
                }
                
                withContext(Dispatchers.IO) {
                    backupFile.writeText(jsonContent)
                }
                
                _operationResult.emit(SettingsResult.Success("备份成功: ${backupFile.name}"))
            } catch (e: Exception) {
                _operationResult.emit(SettingsResult.Error("备份失败: ${e.message}"))
            }
        }
    }
    
    fun restoreData(uri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val content = inputStream?.bufferedReader()?.readText() ?: throw Exception("无法读取文件")
                    inputStream.close()
                    
                    // Parse and restore
                    val students = parseStudentJson(content)
                    studentDao.insertStudents(students)
                }
                
                _operationResult.emit(SettingsResult.Success("恢复成功"))
            } catch (e: Exception) {
                _operationResult.emit(SettingsResult.Error("恢复失败: ${e.message}"))
            }
        }
    }
    
    private fun parseStudentJson(content: String): List<StudentEntity> {
        val students = mutableListOf<StudentEntity>()
        val regex = Regex("""\{"id":\s*(\d+),\s*"name":\s*"([^"]+)",\s*"orderIndex":\s*(\d+)}""")
        
        regex.findAll(content).forEach { match ->
            val id = match.groupValues[1].toLong()
            val name = match.groupValues[2]
            val orderIndex = match.groupValues[3].toInt()
            students.add(StudentEntity(id = id, name = name, orderIndex = orderIndex))
        }
        
        return students
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear database
                AppDatabase.closeDatabase()
                
                // Delete database file
                val dbFile = context.getDatabasePath("student_meal_database")
                if (dbFile.exists()) {
                    dbFile.delete()
                }
                
                // Reinitialize
                AppDatabase.getDatabase(context)
                
                _operationResult.emit(SettingsResult.Success("数据已清除"))
            } catch (e: Exception) {
                _operationResult.emit(SettingsResult.Error("清除失败: ${e.message}"))
            }
        }
    }
}

sealed class SettingsResult {
    data class Success(val message: String) : SettingsResult()
    data class Error(val message: String) : SettingsResult()
}
