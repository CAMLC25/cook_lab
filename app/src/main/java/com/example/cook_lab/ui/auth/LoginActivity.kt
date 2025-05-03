package com.example.cook_lab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.databinding.ActivityLoginBinding
import com.example.cook_lab.ui.auth.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập sự kiện nhấn cho nút Sign In
        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            Toast.makeText(this, "Đăng nhập với: $email", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic kiểm tra đăng nhập (VD: gọi API, xác thực dữ liệu) tại đây
        }

        // Thiết lập sự kiện nhấn cho liên kết Forgot Password
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Mở màn hình quên mật khẩu", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình quên mật khẩu tại đây
        }

        // Thiết lập sự kiện nhấn cho liên kết Sign Up
        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            // TODO: Bổ sung logic thêm nếu cần trước khi chuyển sang RegisterActivity
        }
    }
}