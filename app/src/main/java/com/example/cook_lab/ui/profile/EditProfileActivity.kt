package com.example.cook_lab.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.User
import com.example.cook_lab.databinding.ActivityEditProfileBinding
import com.example.cook_lab.viewmodel.UserDataViewModel
import com.example.cook_lab.viewmodel.UserProfileViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    private lateinit var viewModelUser: UserDataViewModel

    private var selectedImageFile: File? = null
    private var idCookpad: String? = null // Biến lưu trữ ID Cookpad ban đầu từ user

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let {
                val imageFile = File(getRealPathFromURI(it))
                selectedImageFile = imageFile
                binding.avatar.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbarEdit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarEdit.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Không có userId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        viewModelUser = ViewModelProvider(this).get(UserDataViewModel::class.java)
        viewModel.getUserProfile(userId)

        viewModel.userProfile.observe(this) { userProfile ->
            userProfile?.let {
                displayUserProfile(it.user)
            }
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        // Khi người dùng nhấn vào trường ID Cookpad
        binding.idCooklab.setOnClickListener {
            showIdCookpadDialog()
        }

        binding.changeAvatarButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        // Khi người dùng nhấn vào nút "Cập nhật"
        binding.btnSubmit.setOnClickListener {
            val name = binding.usernameInput.text.toString().trim()
            val idCookpad = binding.idCooklab.text.toString().trim() // Lấy giá trị từ TextView id_cooklab
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            // Kiểm tra các trường bắt buộc
            if (name.isEmpty() || email.isEmpty() || idCookpad.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc (tên, email, ID Cookpad)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra định dạng email
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra nếu người dùng nhập mật khẩu mới và xác nhận mật khẩu
            if (password.isNotEmpty() && password != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra nếu người dùng nhập mật khẩu và mật khẩu có ít nhất 6 ký tự
            if (password.isNotEmpty() && password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra ràng buộc cho idCookpad
            if (idCookpad.length !in 4..20 || !idCookpad.matches("^[a-zA-Z0-9_]*$".toRegex())) {
                Toast.makeText(this, "ID Cookpad không hợp lệ. Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Cập nhật thông tin người dùng với giá trị từ id_cooklab
                viewModel.updateUserProfile(userId, name, idCookpad, email, password.takeIf { it.isNotEmpty() }, selectedImageFile)

                // Cập nhật Prefs sau khi API thành công
                Prefs.userJson?.let { json ->
                    val user = Gson().fromJson(json, User::class.java)
                    user.name = name
                    user.email = email
                    user.id_cooklab = idCookpad
                    Prefs.userJson = Gson().toJson(user)
                }

                // Lấy lại thông tin người dùng mới nhất sau khi cập nhật
                viewModel.getUserProfile(userId)
                viewModelUser.getUserData()
                Toast.makeText(this@EditProfileActivity, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()

                // Gửi kết quả để yêu cầu UserProfileActivity reset
                setResult(Activity.RESULT_OK, Intent().putExtra("RESET_REQUIRED", true))
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}"
        return email.matches(emailPattern.toRegex())
    }

    private fun displayUserProfile(user: User) {
        binding.usernameInput.setText(user.name)
        binding.emailInput.setText(user.email)
        binding.idCooklab.setText(user.id_cooklab) // Hiển thị giá trị id_cooklab từ user
        this.idCookpad = user.id_cooklab // Lưu giá trị ban đầu
        Glide.with(this)
            .load(ApiClient.BASE_URL + user.avatar)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .circleCrop()
            .into(binding.avatar)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerResult.launch(intent)
    }

    private fun getRealPathFromURI(contentUri: android.net.Uri): String {
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        cursor?.moveToFirst()
        val idx = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        val filePath = cursor?.getString(idx ?: 0) ?: ""
        cursor?.close()
        return filePath
    }

    private fun showIdCookpadDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_id_cookpad, null)
        builder.setView(dialogView)
        val editTextIdCookpad = dialogView.findViewById<EditText>(R.id.edit_id_cookpad)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmCookId)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Hiển thị giá trị hiện tại của id_cooklab trong edit_id_cookpad
        val currentIdCookpad = binding.idCooklab.text.toString()
        editTextIdCookpad.setText(currentIdCookpad)

        // Set up the dialog
        val dialog = builder.create()
        btnConfirm.setOnClickListener {
            val enteredId = editTextIdCookpad.text.toString().trim()
            if (enteredId.length in 4..20 && enteredId.matches("^[a-zA-Z0-9_]*$".toRegex())) {
                binding.idCooklab.setText(enteredId) // Chỉ cập nhật TextView id_cooklab
                Toast.makeText(this, "ID Cookpad đã được cập nhật trong giao diện.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "ID Cookpad không hợp lệ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        // Gửi kết quả để yêu cầu UserProfileActivity reset khi nhấn nút Back
        setResult(Activity.RESULT_OK, Intent().putExtra("RESET_REQUIRED", true))
        super.onBackPressed()
    }
}