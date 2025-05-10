package com.example.cook_lab.ui.category

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cook_lab.databinding.ActivityCategoryDetailBinding
import com.example.cook_lab.ui.components.CategoryDetailAdapter
import com.example.cook_lab.ui.recipe.RecipeDetailActivity
import com.example.cook_lab.viewmodel.CategoryViewModel

class CategoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryDetailBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Lấy dữ liệu từ Intent
        val categoryId    = intent.getIntExtra("CATEGORY_ID", -1)
        val categoryTitle = intent.getStringExtra("CATEGORY_TITLE")
            ?: "Chi tiết danh mục"
        // 2. Setup toolbar với nút Back và tiêu đề động
        setSupportActionBar(binding.toolbarCategoryDetail)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = categoryTitle
        }
        binding.toolbarCategoryDetail.setNavigationOnClickListener {
            onBackPressed()
        }

        // 3. Khởi tạo adapter với sự kiện click
        adapter = CategoryDetailAdapter(categoryTitle) { selectedRecipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", selectedRecipe.id)
            startActivity(intent)
        }

        // 4. Cấu hình RecyclerView
        binding.recipeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryDetailActivity)
            adapter = this@CategoryDetailActivity.adapter
        }

        // 5. Khởi tạo ViewModel và observe dữ liệu
        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        viewModel.recipes.observe(this) { list ->
            Log.e("CategoryDetailActivity", "Recipes: $list")
            if (list.isEmpty()) {
                Toast.makeText(
                    this,
                    "Không có công thức nào \n trong danh mục này.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            adapter.setData(list)
        }
        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        // 6. Gọi API nếu ID hợp lệ
        if (categoryId != -1) {
            viewModel.fetchRecipesByCategory(categoryId)
        } else {
            Toast.makeText(this, "Danh mục không hợp lệ", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}