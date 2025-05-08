package com.example.cook_lab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.cook_lab.MainActivity
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.LoginResponse
import com.example.cook_lab.data.model.RegisterRequest
import com.example.cook_lab.databinding.ActivityRegisterBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar với nút back
        setSupportActionBar(binding.toolbarRegister)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbarRegister.setNavigationOnClickListener {
            onBackPressed()
        }

        // Nếu đã login, vào thẳng MainActivity
        Prefs.token?.takeIf { it.isNotBlank() }?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Clear error khi user nhập lại email / confirm password
        binding.registerEmailEditText.doAfterTextChanged {
            binding.registerEmailInputLayout.error = null
        }
        binding.registerConfirmPasswordEditText.doAfterTextChanged {
            binding.registerConfirmPasswordInputLayout.error = null
        }

        binding.registerSignUpButton.setOnClickListener {
            val name    = binding.registerNameEditText.text.toString().trim()
            val email   = binding.registerEmailEditText.text.toString().trim()
            val pass    = binding.registerPasswordEditText.text.toString()
            val confirm = binding.registerConfirmPasswordEditText.text.toString()
            val terms   = binding.registerTermsCheckbox.isChecked

            // Validate client-side
            when {
                name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                pass != confirm -> {
                    binding.registerConfirmPasswordInputLayout.error = "Mật khẩu xác nhận không khớp"
                    return@setOnClickListener
                }
                !terms -> {
                    Toast.makeText(this, "Vui lòng chấp nhận điều khoản và điều kiện", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Disable nút, đổi text
            binding.registerSignUpButton.isEnabled = false
            binding.registerSignUpButton.text = "Đang đăng ký..."

            lifecycleScope.launch {
                try {
                    val req = RegisterRequest(
                        name                  = name,
                        email                 = email,
                        password              = pass,
                        password_confirmation = confirm
                    )
                    val response: Response<LoginResponse> =
                        ApiClient.apiService.register(req)

                    if (response.isSuccessful) {
                        // Thành công 2xx
                        val body = response.body()!!
                        Prefs.token    = body.token
                        Prefs.userJson = gson.toJson(body.user)

                        Toast.makeText(
                            this@RegisterActivity,
                            "Đăng ký thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Lỗi 4xx/5xx
                        val raw = response.errorBody()?.string()
                        val errorMsg = raw?.let {
                            try {
                                val json = JSONObject(it)
                                val errs = json.optJSONObject("errors")
                                errs?.optJSONArray("email")?.optString(0)
                                    ?: json.optString("message", "Đăng ký thất bại")
                            } catch (_: Exception) {
                                "Đăng ký thất bại"
                            }
                        } ?: "Đăng ký thất bại"

                        // Hiển thị toast
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()

                        // Nếu lỗi email, show ngay dưới ô email
                        if (errorMsg.contains("email", ignoreCase = true)) {
                            binding.registerEmailInputLayout.error = errorMsg
                        }
                    }
                } catch (e: Exception) {
                    // Network hoặc lỗi bất ngờ
                    val msg = "Không thể kết nối đến server"
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                } finally {
                    // Reset button
                    binding.registerSignUpButton.isEnabled = true
                    binding.registerSignUpButton.text = "Đăng ký"
                }
            }
        }

        binding.registerTermsText.setOnClickListener {
            Toast.makeText(this, "Điều khoản & Điều kiện", Toast.LENGTH_SHORT).show()
        }

        binding.registerSignInLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}
