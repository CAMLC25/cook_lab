package com.example.cook_lab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cook_lab.data.api.UserProfileResponse
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.databinding.ActivityKhoBinding
import com.example.cook_lab.ui.components.KhoRecipeAdapter
import com.example.cook_lab.ui.components.UserRecipeAdapter
import com.example.cook_lab.ui.recipe.CreateRecipeActivity
import com.example.cook_lab.ui.recipe.EditRecipeActivity
import com.example.cook_lab.viewmodel.UserProfileViewModel

class KhoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKhoBinding
    private lateinit var viewModelUser: UserProfileViewModel
    private var visibleRecipeCount = 1 // Số lượng công thức hiển thị ban đầu
    private var allRecipes: List<Recipe> = emptyList()
    private lateinit var recipeUserAdapter: KhoRecipeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy userId từ Intent
        val userId = intent.getIntExtra("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không có userId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Khởi tạo ViewModel
        viewModelUser = ViewModelProvider(this).get(UserProfileViewModel::class.java)
        viewModelUser.getUserProfile(userId)
        viewModelUser.getSavedRecipes(userId)

        // Quan sát dữ liệu trả về từ ViewModel
        viewModelUser.userProfile.observe(this) { response ->
            response?.let {
                displayUserProfile(it)
                binding.loadingLayout.visibility = View.GONE
            }
        }

        // Quan sát kết quả trả về từ ViewModel
        viewModelUser.savedRecipes.observe(this) { recipes ->
            if (recipes != null && recipes.isNotEmpty()) {
                allRecipes = recipes // Lưu toàn bộ danh sách công thức
                displaySavedRecipes() // Hiển thị danh sách ban đầu
                Log.e("KhoActivity", "Recipes: $recipes") // Log để kiểm tra dữ liệu
            } else {
                Toast.makeText(this, "Không có công thức nào đã lưu.", Toast.LENGTH_SHORT).show()
            }
        }

        // Quan sát kết quả trả về từ việc xóa công thức
        viewModelUser.deleteRecipeResponse.observe(this) { response ->
            if (response != null) {
                Toast.makeText(this, "Xóa công thức thành công!", Toast.LENGTH_SHORT).show()
                // Sau khi xóa, tải lại danh sách công thức
                viewModelUser.getUserProfile(userId)
            }
        }

        // Quan sát lỗi từ ViewModel
        viewModelUser.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            binding.loadingLayout.visibility = View.GONE
        }

        // Thiết lập sự kiện chọn mục trong Bottom Navigation Bar
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this::class.java.name != "com.example.cook_lab.MainActivity") {
                        Toast.makeText(this, "Đã chọn Trang chủ", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Đã ở Trang chủ", Toast.LENGTH_SHORT).show()
                        recreate()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    true
                }
                R.id.nav_library -> {
                    if (this::class.java.name != "com.example.cook_lab.KhoActivity") {
                        Toast.makeText(this, "Đã chọn Kho món ngon", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, KhoActivity::class.java))
                    } else {
                        Toast.makeText(this, "Đã ở Kho món ngon", Toast.LENGTH_SHORT).show()
                        recreate()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    true
                }
                else -> false
            }
        }

        // Thêm công thức mới
        binding.addRecipeButton.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }

        // Cấu hình Adapter cho RecyclerView danh sách công thức đã lưu
        recipeUserAdapter = KhoRecipeAdapter(emptyList()) { recipeId ->
            removeSavedRecipe(recipeId)
        }

        // Cập nhật RecyclerView với các công thức
        binding.saveRecipesRecyclerView.adapter = recipeUserAdapter
        Log.e("KhoActivity", "Adapter configured")

        // Xử lý sự kiện nhấn nút "Xem thêm"
        binding.btnLoadMore.setOnClickListener {
            visibleRecipeCount += 2
            if (visibleRecipeCount >= allRecipes.size) {
                visibleRecipeCount = allRecipes.size
                binding.btnLoadMore.visibility = View.GONE
            }
            displaySavedRecipes() // Cập nhật danh sách hiển thị
        }
    }

    private fun displayUserProfile(userProfile: UserProfileResponse) {
        binding.tvRecipeCount.text = "( ${userProfile.recipes.size} món )"
        val adapter = UserRecipeAdapter(userProfile.recipes, { recipeId ->
            viewModelUser.deleteRecipe(recipeId) // Xóa công thức
        }, { recipeId ->
            val intent = Intent(this, EditRecipeActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        })

        binding.newRecipesForYouRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@KhoActivity)
            this.adapter = adapter
        }
    }

    private fun displaySavedRecipes() {
        binding.tvSavedRecipeCount.text = "( ${allRecipes.size} món )"

        // Đảm bảo visibleRecipeCount không lớn hơn allRecipes.size
        if (visibleRecipeCount > allRecipes.size) {
            visibleRecipeCount = allRecipes.size
        }
        // Đảm bảo visibleRecipeCount không nhỏ hơn 1 nếu allRecipes không rỗng
        if (allRecipes.isNotEmpty() && visibleRecipeCount < 1) {
            visibleRecipeCount = 1
        }

        val recipesToShow = if (visibleRecipeCount >= allRecipes.size) {
            allRecipes // Hiển thị tất cả nếu vượt quá số lượng
        } else {
            allRecipes.subList(0, visibleRecipeCount)
        }

        if (::recipeUserAdapter.isInitialized) {
            recipeUserAdapter.updateRecipes(recipesToShow)
        }

        binding.saveRecipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@KhoActivity)
        }

        binding.btnLoadMore.visibility = if (visibleRecipeCount >= allRecipes.size) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun removeSavedRecipe(recipeId: Int) {
        viewModelUser.deleteRecipe(recipeId)
        allRecipes = allRecipes.filter { it.id != recipeId }
        displaySavedRecipes()
    }
}
