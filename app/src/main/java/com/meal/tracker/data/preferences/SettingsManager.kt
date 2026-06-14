package com.meal.tracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * 统一管理应用设置项（SharedPreferences）的读写。
 * 其它模块通过 [exportPath] StateFlow 订阅变更，不再各自接触 Context。
 *
 * 导出路径策略：仅支持应用专属外部目录（`getExternalFilesDir(null)/exports/`），
 * 无需任何运行时权限。SAF 目录在多 Android 版本下行为差异大、调试困难，
 * 故改为「查看当前路径 / 一键复制 / 一键重置」的三按钮 UI。
 */
class SettingsManager(context: Context) {

    private val appContext: Context = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _exportPath = MutableStateFlow(loadExportPath())
    val exportPath: StateFlow<String> = _exportPath.asStateFlow()

    /** 解析并确保目录存在，返回当前生效的导出目录。 */
    fun resolveExportDir(): File {
        val dir = File(_exportPath.value)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun setExportPath(path: String) {
        val normalized = path.trim().ifEmpty { defaultPath() }
        // 校验：app 必须能创建并写入该目录，否则保留旧值
        val target = File(normalized)
        if (!target.exists() && !target.mkdirs()) {
            throw IllegalStateException("无法创建目录：$normalized（app 无写权限）")
        }
        if (target.exists() && !target.canWrite()) {
            throw IllegalStateException("目录不可写：$normalized")
        }
        prefs.edit().putString(KEY_EXPORT_PATH, normalized).apply()
        _exportPath.value = normalized
    }

    /** 把导出目录重置为应用专属外部目录（`/Android/data/包名/files/exports/`）。 */
    fun resetExportPath() {
        prefs.edit().remove(KEY_EXPORT_PATH).apply()
        _exportPath.value = defaultPath()
        File(_exportPath.value).mkdirs()
    }

    private fun defaultPath(): String {
        val base = appContext.getExternalFilesDir(null) ?: appContext.filesDir
        return File(base, "exports").absolutePath
    }

    private fun loadExportPath(): String {
        val saved = prefs.getString(KEY_EXPORT_PATH, null)
        if (!saved.isNullOrBlank()) return saved
        return defaultPath()
    }

    companion object {
        private const val PREFS_NAME = "settings"
        private const val KEY_EXPORT_PATH = "export_path"
    }
}
