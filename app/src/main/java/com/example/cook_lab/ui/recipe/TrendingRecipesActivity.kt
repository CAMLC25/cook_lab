package com.example.cook_lab.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cook_lab.databinding.ActivityNewRecipesBinding
import com.example.cook_lab.databinding.ActivityTrendingRecipesBinding
import com.example.cook_lab.ui.BaseActivity
import com.example.cook_lab.ui.components.NewRecipesAdapter
import com.example.cook_lab.viewmodel.RecipeViewModel

class TrendingRecipesActivity : BaseActivity() {
    private lateinit var binding: ActivityTrendingRecipesBinding
    private lateinit var viewModel: RecipeViewModel
    private lateinit var adapter: NewRecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendingRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar với nút Back
        setSupportActionBar(binding.toolbarTrendingRecipes)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Những món ăn thịnh hành!!!"
        }
        binding.toolbarTrendingRecipes.setNavigationOnClickListener {
            onBackPressed()
        }

        // Adapter & RecyclerView (grid 2 cột)
        adapter = NewRecipesAdapter(
            onItemClick = { recipe ->
                // Mở màn hình chi tiết, truyền ID
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            },
            onBookmarkClick = { recipe ->
                Toast.makeText(this, "Đã lưu: ${recipe.title}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.recyclerTrendingRecipes.apply {
            layoutManager = GridLayoutManager(this@TrendingRecipesActivity, 2)
            adapter = this@TrendingRecipesActivity.adapter
        }

        // ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        viewModel.trendingRecipes.observe(this) { list ->
            if (list.isEmpty()) {
                Toast.makeText(this, "Chưa có công thức mới nào", Toast.LENGTH_SHORT).show()
            }
            adapter.submitList(list)
        }
        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchTrendingRecipes()

        binding.addRecipeButton.setOnClickListener {
            if (!requireLogin()) return@setOnClickListener
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }
    }
}
