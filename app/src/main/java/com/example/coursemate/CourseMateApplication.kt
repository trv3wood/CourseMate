package com.example.coursemate

import android.app.Application
import com.example.coursemate.di.AppContainer

class CourseMateApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
