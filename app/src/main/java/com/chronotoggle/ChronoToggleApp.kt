package com.chronotoggle

import android.app.Application
import com.chronotoggle.data.db.AppDatabase

class ChronoToggleApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
