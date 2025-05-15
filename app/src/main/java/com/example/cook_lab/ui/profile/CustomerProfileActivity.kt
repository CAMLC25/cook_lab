package com.example.cook_lab.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.CustomerProfileResponse
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.User
import com.example.cook_lab.databinding.ActivityCustomerProfileBinding
import com.example.cook_lab.ui.BaseActivity
import com.example.cook_lab.ui.components.CustomerRecipesAdapter
import com.example.cook_lab.ui.components.UserRecipeAdapter
import com.example.cook_lab.ui.recipe.CreateRecipeActivity
import com.example.cook_lab.ui.recipe.RecipeDetailActivity
import com.example.cook_lab.viewmodel.CustomerViewModel
import com.example.cook_lab.viewmodel.FollowViewModel
import com.google.gson.Gson

class CustomerProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityCustomerProfileBinding
    private lateinit var viewModel: CustomerViewModel
    private val followViewModel by lazy {
        ViewModelProvider(this)[FollowViewModel::class.java]
    }

    private var isFollowing = false
    private val gson = Gson()

    companion object {
        const val REQUEST_EDIT_PROFILE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarProfile.setNavigationOnClickListener {
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
        viewModel = ViewModelProvider(this).get(CustomerViewModel::class.java)
        viewModel.getCustomerProfile(userId)

        // Quan sát dữ liệu trả về từ ViewModel
        viewModel.customerProfile.observe(this) { response ->
            response?.let {
                displayCustomerProfile(it)
                binding.loadingLayout.visibility = View.GONE
            }
        }

        // Quan sát lỗi từ ViewModel
        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            binding.loadingLayout.visibility = View.GONE
        }

        binding.addRecipeButton.setOnClickListener {
            if (!requireLogin()) return@setOnClickListener
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }

        // Observe follow status
        followViewModel.isFollowing.observe(this) { isFollowed ->
            isFollowing = isFollowed
            updateFollowButton()
        }

        // Observe follow errors
        followViewModel.error.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        // Handle Follow/Unfollow action
        viewModel.customerProfile.observe(this) { r ->
            // Lấy ID của người đăng công thức
            val authorId = r.user.id
            // Lấy ID người dùng hiện tại
            val currentUserId = Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id }

            // Kiểm tra xem người dùng hiện tại có phải là tác giả công thức không
            if (authorId == currentUserId) {
                binding.btnFollow.visibility = View.GONE
                Log.d("RecipeDetailActivity", "Hiding follow button: user is the author (authorId=$authorId)")
            } else {
                binding.btnFollow.visibility = View.VISIBLE
                // Kiểm tra trạng thái follow
                followViewModel.checkIfUserFollows(authorId)
                Log.d("RecipeDetailActivity", "Checking follow status for authorId=$authorId")

                // Handle Follow/Unfollow button click
                binding.btnFollow.setOnClickListener {
                    if (!requireLogin()) return@setOnClickListener
                    if (isFollowing) {
                        followViewModel.unfollowUser(authorId)
                        Log.d("RecipeDetailActivity", "Unfollow user: authorId=$authorId")
                    } else {
                        followViewModel.followUser(authorId)
                        Log.d("RecipeDetailActivity", "Follow user: authorId=$authorId")
                    }

                    // Lấy lại thông tin người dùng và cập nhật giao diện
                    viewModel.getCustomerProfile(authorId)

                    // Quan sát lại dữ liệu đã cập nhật
                    viewModel.customerProfile.observe(this) { updatedProfile ->
                        updatedProfile?.let {
                            displayCustomerProfile(it)
                            updateFollowButton()
                        }
                    }
                }
            }
        }
    }

    private fun displayCustomerProfile(customerProfile: CustomerProfileResponse) {
        val user = customerProfile.user

        // Hiển thị thông tin người dùng
        binding.userName.text = user.name
        binding.userHandle.text = "@${user.id_cooklab}"
        binding.followersCount.text = user.followers_count.toString()
        binding.followingCount.text = user.following_count.toString()

        // Load avatar
        Glide.with(this)
            .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .circleCrop()
            .into(binding.avatarImage)

        // Số lượng công thức
        binding.tvRecipeCount.text = "${customerProfile.recipes.size} món"

        // Setup RecyclerView cho công thức của người dùng
        val adapter = CustomerRecipesAdapter(customerProfile.recipes) { recipeId ->
            // Khi nhấn vào công thức, mở màn hình chi tiết công thức
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }

        binding.rvUserRecipes.apply {
            layoutManager = LinearLayoutManager(this@CustomerProfileActivity)
            this.adapter = adapter
        }
    }

    private fun resetToInitialState() {
        binding.loadingLayout.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            val userId = intent.getIntExtra("USER_ID", -1)
            if (userId != -1) {
                viewModel.getCustomerProfile(userId)
                binding.rvUserRecipes.scrollToPosition(0)
                binding.loadingLayout.visibility = View.GONE
            }
        }, 2000)
    }

    private fun updateFollowButton() {
        // Cập nhật trạng thái của nút "Theo dõi" hoặc "Đã theo dõi"
        val followText = if (isFollowing) "Đã theo dõi" else "Theo dõi"
        binding.btnFollow.text = followText
    }
}

