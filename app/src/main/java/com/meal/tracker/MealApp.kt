package com.meal.tracker

import android.app.Application
import com.meal.tracker.di.ServiceLocator

class MealApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
