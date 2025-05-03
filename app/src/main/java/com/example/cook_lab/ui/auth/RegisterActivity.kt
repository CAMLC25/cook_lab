package com.example.cook_lab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập sự kiện nhấn cho nút Sign Up
        binding.registerSignUpButton.setOnClickListener {
            val name = binding.registerNameEditText.text.toString()
            val email = binding.registerEmailEditText.text.toString()
            val password = binding.registerPasswordEditText.text.toString()
            val confirmPassword = binding.registerConfirmPasswordEditText.text.toString()
            val termsAccepted = binding.registerTermsCheckbox.isChecked

            if (!termsAccepted) {
                Toast.makeText(this, "Vui lòng chấp nhận điều khoản và điều kiện", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Đăng ký với: $email", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic đăng ký (VD: gọi API, lưu dữ liệu người dùng) tại đây
        }

        // Thiết lập sự kiện nhấn cho liên kết Terms & Conditions
        binding.registerTermsText.setOnClickListener {
            Toast.makeText(this, "Mở màn hình điều khoản và điều kiện", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình điều khoản và điều kiện tại đây
        }

        // Thiết lập sự kiện nhấn cho liên kết Sign In
        binding.registerSignInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            // TODO: Bổ sung logic thêm nếu cần trước khi chuyển sang LoginActivity
        }
    }
}