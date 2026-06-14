package com.meal.tracker.di

import android.content.Context
import com.meal.tracker.data.database.AppDatabase
import com.meal.tracker.data.export.ExportRepository
import com.meal.tracker.data.preferences.SettingsManager
import com.meal.tracker.data.repository.MealRepository

/**
 * 轻量级依赖容器，避免引入 Hilt 带来的构建复杂度。
 * 必须在 Application.onCreate 中调用 [init] 完成初始化。
 */
object ServiceLocator {

    @Volatile
    private var initialized: Boolean = false

    lateinit var database: AppDatabase
        private set

    lateinit var mealRepository: MealRepository
        private set

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var exportRepository: ExportRepository
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appContext = context.applicationContext
            database = AppDatabase.getDatabase(appContext)
            mealRepository = MealRepository(database.studentDao(), database.mealRecordDao())
            settingsManager = SettingsManager(appContext)
            exportRepository = ExportRepository(
                mealRepository = mealRepository,
                settingsManager = settingsManager
            )
            initialized = true
        }
    }
}
