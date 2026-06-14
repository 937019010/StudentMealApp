package com.meal.tracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meal.tracker.data.entity.StudentEntity
import com.meal.tracker.data.preferences.SettingsManager
import com.meal.tracker.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val settingsManager: SettingsManager = ServiceLocator.settingsManager
    private val studentDao = ServiceLocator.database.studentDao()

    /** 直接转发 SettingsManager 的路径，UI 层无需关心持久化细节。 */
    val exportPath: StateFlow<String> = settingsManager.exportPath

    private val _operationResult = MutableSharedFlow<SettingsResult>()
    val operationResult: SharedFlow<SettingsResult> = _operationResult.asSharedFlow()

    fun setExportPath(path: String) {
        settingsManager.setExportPath(path)
    }

    fun resetExportPath() {
        settingsManager.resetExportPath()
    }

    /** 保留兼容：以往 UI 会在进入页面时调用 loadSettings，如今路径已实时同步，无需操作。 */
    @Suppress("unused")
    fun loadSettings() = Unit

    fun backupData() {
        viewModelScope.launch {
            runCatching { writeBackup() }
                .onSuccess { file ->
                    _operationResult.emit(SettingsResult.Success("备份成功: ${file.name}"))
                }
                .onFailure { e ->
                    _operationResult.emit(SettingsResult.Error("备份失败: ${e.message}"))
                }
        }
    }

    fun restoreData(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val text = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.readText()
                        ?: throw IllegalStateException("无法读取文件")
                    val students = parseStudentJson(text)
                    studentDao.insertStudents(students)
                }
            }.onSuccess {
                _operationResult.emit(SettingsResult.Success("恢复成功"))
            }.onFailure { e ->
                _operationResult.emit(SettingsResult.Error("恢复失败: ${e.message}"))
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            runCatching {
                com.meal.tracker.data.database.AppDatabase.closeDatabase()
                context.getDatabasePath("student_meal_database").delete()
                com.meal.tracker.data.database.AppDatabase.getDatabase(context)
            }.onSuccess {
                _operationResult.emit(SettingsResult.Success("数据已清除"))
            }.onFailure { e ->
                _operationResult.emit(SettingsResult.Error("清除失败: ${e.message}"))
            }
        }
    }

    private suspend fun writeBackup(): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val target = File(backupDir, "backup_${System.currentTimeMillis()}.json")
        val students = studentDao.getAllStudentsList()
        target.writeText(buildJson(students))
        target
    }

    private fun buildJson(students: List<StudentEntity>): String = buildString {
        appendLine("[")
        students.forEachIndexed { index, s ->
            appendLine("  {")
            appendLine("    \"id\": ${s.id},")
            appendLine("    \"name\": \"${s.name}\",")
            appendLine("    \"orderIndex\": ${s.orderIndex}")
            append("  }")
            appendLine(if (index < students.size - 1) "," else "")
        }
        appendLine("]")
    }

    private fun parseStudentJson(content: String): List<StudentEntity> {
        val regex = Regex("""\{"id":\s*(\d+),\s*"name":\s*"([^"]+)",\s*"orderIndex":\s*(\d+)}""")
        return regex.findAll(content).map { match ->
            StudentEntity(
                id = match.groupValues[1].toLong(),
                name = match.groupValues[2],
                orderIndex = match.groupValues[3].toInt()
            )
        }.toList()
    }
}

sealed class SettingsResult {
    data class Success(val message: String) : SettingsResult()
    data class Error(val message: String) : SettingsResult()
}
