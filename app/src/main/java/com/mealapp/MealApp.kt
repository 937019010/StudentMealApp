package com.mealapp

import android.app.Application
import com.mealapp.data.database.AppDatabase

class MealApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize database
        AppDatabase.getDatabase(this)
    }
}
