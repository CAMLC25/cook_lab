package com.example.cook_lab.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.cook_lab.MainActivity
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.LoginRequest
import com.example.cook_lab.data.model.LoginResponse
import com.example.cook_lab.databinding.ActivityLoginBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar với nút back
        setSupportActionBar(binding.toolbarLogin)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbarLogin.setNavigationOnClickListener { onBackPressed() }

        // Nếu đã login, vào thẳng Main
        Prefs.token?.takeIf { it.isNotBlank() }?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.signInButton.setOnClickListener {
            val email    = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            // Reset trước khi validate
            binding.emailInputLayout.error = null

            if (email.isEmpty() || password.isEmpty()) {
                binding.emailInputLayout.error = if (email.isEmpty()) "Chưa nhập email" else null
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInputLayout.error = "Email không đúng định dạng"
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.signInButton.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp: LoginResponse = ApiClient.apiService.login(
                        LoginRequest(email, password)
                    )

                    if (!resp.success) {
                        // API 200 nhưng success=false
                        binding.emailInputLayout.error = resp.message
                    } else {
                        // Thành công...
                        Prefs.token    = resp.token
                        Prefs.userJson = gson.toJson(resp.user)
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }

                } catch (e: Exception) {
                    if (e is HttpException && e.code() == 401) {
                        // Lỗi credentials -> hiển thị dưới ô email
                        val raw = e.response()?.errorBody()?.string()
                        val msg = try {
                            JSONObject(raw).optString("message", "Email hoặc mật khẩu không chính xác.")
                        } catch (_: Exception) {
                            "Email hoặc mật khẩu không chính xác."
                        }
                        binding.emailInputLayout.error = msg
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            if (e is HttpException) "Lỗi máy chủ: ${e.code()}" else "Không thể kết nối đến server",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("LoginActivity_ngu", "e", e)
                    }
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.signInButton.isEnabled = true
                }
            }
        }

        // Xóa lỗi khi người dùng sửa email
        binding.emailEditText.doAfterTextChanged {
            binding.emailInputLayout.error = null
        }



        // Chuyển sang Register nếu cần
        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
}
