package com.example.cook_lab

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application() {
    companion object { lateinit var instance: App }
    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this)
    }
}