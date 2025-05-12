package com.example.cook_lab.ui

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.KhoActivity
import com.example.cook_lab.MainActivity
import com.example.cook_lab.R
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.ui.components.LoginPromptDialog
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initBottomNav()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        initBottomNav()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        initBottomNav()
    }

    private fun initBottomNav() {
        findViewById<BottomNavigationView?>(R.id.bottomNavigationView)
            ?.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        true
                    }
                    R.id.nav_library -> {
                        if (!requireLogin()) return@setOnItemSelectedListener false
                        startActivity(Intent(this, KhoActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
    }

    protected fun requireLogin(): Boolean {
        if (Prefs.token.isNullOrEmpty() || Prefs.userJson == null) {
            LoginPromptDialog(this).show()
            Log.d("BaseActivity", "Action blocked: user not logged in")
            return false
        }
        return true
    }
}