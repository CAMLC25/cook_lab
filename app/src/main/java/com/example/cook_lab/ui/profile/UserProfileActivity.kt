package com.example.cook_lab.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.UserProfileResponse
import com.example.cook_lab.databinding.ActivityUserProfileBinding
import com.example.cook_lab.ui.BaseActivity
import com.example.cook_lab.ui.components.UserRecipeAdapter
import com.example.cook_lab.viewmodel.UserProfileViewModel

class UserProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel

    companion object {
        const val REQUEST_EDIT_PROFILE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý toolbar với nút back
        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarProfile.setNavigationOnClickListener {
            // Gửi kết quả RESET_REQUIRED khi nhấn nút Back
            setResult(Activity.RESULT_OK, Intent().putExtra("RESET_REQUIRED", true))
            finish()
        }

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không có userId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        viewModel.getUserProfile(userId)

        // Observe user profile data
        viewModel.userProfile.observe(this) { response ->
            response?.let {
                displayUserProfile(it)
                // Ẩn loadingLayout khi dữ liệu được tải
                binding.loadingLayout.visibility = View.GONE
            }
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            // Ẩn loadingLayout nếu có lỗi
            binding.loadingLayout.visibility = View.GONE
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivityForResult(intent, REQUEST_EDIT_PROFILE)
        }
    }

    private fun displayUserProfile(userProfile: UserProfileResponse) {
        val user = userProfile.user

        // 1. Hiển thị thông tin user
        binding.userName.text = user.name
        binding.userHandle.text = "@${user.id_cooklab}"
        binding.followersCount.text = user.followers_count.toString()
        binding.followingCount.text = user.following_count.toString()

        // 2. Load avatar
        Glide.with(this)
            .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .circleCrop()
            .into(binding.avatarImage)

        // 3. Số lượng công thức
        binding.tvRecipeCount.text = "${userProfile.recipes.size} món"

        // 4. Setup RecyclerView đúng cách
        val adapter = UserRecipeAdapter(userProfile.recipes)
        binding.rvUserRecipes.apply {
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            this.adapter = adapter
        }

        Log.d("UserProfile", "Recipes: ${userProfile.recipes.size}")
    }

    private fun resetToInitialState() {
        // Hiển thị loadingLayout
        binding.loadingLayout.visibility = View.VISIBLE

        // Trì hoãn 2 giây để hiển thị hiệu ứng loading
        Handler(Looper.getMainLooper()).postDelayed({
            val userId = intent.getIntExtra("USER_ID", -1)
            if (userId != -1) {
                // Gọi lại API để làm mới dữ liệu
                viewModel.getUserProfile(userId)
                // Cuộn RecyclerView về đầu
                binding.rvUserRecipes.scrollToPosition(0)
                // Ẩn loadingLayout sau khi reset
                binding.loadingLayout.visibility = View.GONE
            }
        }, 2000) // Trì hoãn 2000ms (2 giây)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            val resetRequired = data?.getBooleanExtra("RESET_REQUIRED", false) ?: false
            if (resetRequired) {
                resetToInitialState()
                Toast.makeText(this, "Đã đến hồ sơ người dùng", Toast.LENGTH_SHORT).show()
            }
        }
    }
}