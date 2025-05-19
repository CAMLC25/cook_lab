package com.example.cook_lab.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.cook_lab.MainActivity
import com.example.cook_lab.R
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.databinding.ActivityStartBinding
import com.example.cook_lab.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton

class StartActivity : AppCompatActivity() {
    private val binding: ActivityStartBinding by lazy {
        ActivityStartBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Nếu đã có token, chuyển thẳng vào MainActivity
        if (!Prefs.token.isNullOrEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.startCookingButton.setOnClickListener {
//            showWelcomeDialog()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun showWelcomeDialog() {
        // Tạo dialog
        val dialog = Dialog(this).apply {
            // Dùng layout tuỳ biến dialog_start.xml
            setContentView(R.layout.dialog_start)
            // Nền trong suốt để bo góc card nổi rõ
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(true)
        }

        // Bỏ qua
        dialog.findViewById<MaterialButton>(R.id.btnSkip)
            ?.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }

        // Đăng nhập ngay
        dialog.findViewById<MaterialButton>(R.id.btnLoginNow)
            ?.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
            }

        dialog.show()
    }
}
