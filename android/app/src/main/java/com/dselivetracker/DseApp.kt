package com.dselivetracker

import android.app.Application
import com.dselivetracker.data.local.AppDatabase

class DseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
