package com.example.cook_lab.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.KhoActivity
import com.example.cook_lab.MainActivity
import com.example.cook_lab.R
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.User
import com.example.cook_lab.ui.components.LoginPromptDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

abstract class BaseActivity : AppCompatActivity() {
    private val gson = Gson()

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
                val userId = Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id }
                when (item.itemId) {
                    R.id.nav_home -> {
                        if (this::class.java.name != "com.example.cook_lab.MainActivity") {
                            Toast.makeText(this, "Đã chọn Trang chủ", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            Toast.makeText(this, "Đã ở Trang chủ", Toast.LENGTH_SHORT).show()
                            recreate()
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        true
                    }
                    R.id.nav_library -> {
                        if (this::class.java.name != "com.example.cook_lab.KhoActivity") {
                            Toast.makeText(this, "Đã chọn Kho món ngon", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, KhoActivity::class.java)
                            intent.putExtra("USER_ID", userId) // Truyền userId khi vào KhoActivity
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Đã ở Kho món ngon", Toast.LENGTH_SHORT).show()
                            recreate()
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        true
                    }
                    else -> false
                }
            }
    }

    protected fun requireLogin(): Boolean {
        val userId = Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id }

        if (Prefs.token.isNullOrEmpty() || Prefs.userJson == null) {
            LoginPromptDialog(this).show()
            Log.d("BaseActivity", "Action blocked: user not logged in")
            return false
        }
        // Truyền userId vào Intent nếu người dùng đã đăng nhập
        intent.putExtra("USER_ID", userId)
        return true
    }

}