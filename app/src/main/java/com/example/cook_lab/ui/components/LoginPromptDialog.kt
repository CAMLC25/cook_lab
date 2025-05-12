package com.example.cook_lab.ui.components

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.example.cook_lab.R
import com.example.cook_lab.databinding.LayoutLoginPromptDialogBinding
import com.example.cook_lab.ui.auth.LoginActivity

class LoginPromptDialog(
    context: Context,
    private val message: String = "Cook Lab muốn bạn đăng nhập để thực hiện chức năng này!",
    private val onContinue: (() -> Unit)? = null
) : Dialog(context, R.style.DialogAnimation) {

    private lateinit var binding: LayoutLoginPromptDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LayoutLoginPromptDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure window attributes
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = (displayMetrics.widthPixels * 0.7).toInt() // 70% chiều rộng màn hình
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            attributes.gravity = Gravity.CENTER
            attributes.dimAmount = 0.5f // Hiệu ứng mờ nền
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        // Set message
        binding.tvDialogMessage.text = message

        // Cancel button
        binding.btnDialogCancel.setOnClickListener {
            Log.d("LoginPromptDialog", "Dialog cancelled")
            dismiss()
        }

        // Continue button
        binding.btnDialogContinue.setOnClickListener {
            Log.d("LoginPromptDialog", "Navigating to LoginActivity")
            onContinue?.invoke() ?: context.startActivity(Intent(context, LoginActivity::class.java))
            dismiss()
        }

        // Make dialog non-cancelable by back press
        setCancelable(false)
    }
}