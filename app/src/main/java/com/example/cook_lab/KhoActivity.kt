package com.example.cook_lab

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.databinding.ActivityKhoBinding

class KhoActivity: AppCompatActivity() {
    private lateinit var binding: ActivityKhoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập sự kiện chọn mục trong Bottom Navigation Bar
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Kiểm tra nếu đã ở MainActivity thì không cần tạo lại Intent
                    if (this::class.java.name != "com.example.cook_lab.MainActivity") {
                        Toast.makeText(this, "Đã chọn Trang chủ", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // Nếu đã ở MainActivity, có thể reload lại
                        Toast.makeText(this, "Đã ở Trang chủ", Toast.LENGTH_SHORT).show()
                        recreate()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    true
                }
                R.id.nav_library -> {
                    // Kiểm tra nếu đã ở KhoActivity thì không cần tạo lại Intent
                    if (this::class.java.name != "com.example.cook_lab.KhoActivity") {
                        Toast.makeText(this, "Đã chọn Kho món ngon", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, KhoActivity::class.java))
                    } else {
                        // Nếu đã ở KhoActivity, có thể reload lại
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

}