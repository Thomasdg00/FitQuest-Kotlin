package com.univpm.fitquest

import android.app.Application
import com.univpm.fitquest.di.AppContainer

class FitQuestApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
