package com.example.cook_lab.data.api

import android.content.Context
import com.example.cook_lab.App

object Prefs {
    private val prefs by lazy {
        App.instance.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("KEY_TOKEN", null)
        set(v) = prefs.edit().putString("KEY_TOKEN", v).apply()

    // Lưu JSON của User
    var userJson: String?
        get() = prefs.getString("KEY_USER", null)
        set(v) = prefs.edit().putString("KEY_USER", v).apply()

    fun clear() = prefs.edit().clear().apply()
}
