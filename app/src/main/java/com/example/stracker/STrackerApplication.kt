package com.example.stracker

import android.app.Application
import timber.log.Timber

class STrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}