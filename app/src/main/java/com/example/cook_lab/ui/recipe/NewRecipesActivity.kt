package com.example.cook_lab.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cook_lab.databinding.ActivityNewRecipesBinding
import com.example.cook_lab.ui.components.NewRecipesAdapter
import com.example.cook_lab.viewmodel.RecipeViewModel

class NewRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewRecipesBinding
    private lateinit var viewModel: RecipeViewModel
    private lateinit var adapter: NewRecipesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar với nút Back
        setSupportActionBar(binding.toolbarNewRecipes)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Món mới lên sóng"
        }
        binding.toolbarNewRecipes.setNavigationOnClickListener {
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
        binding.recyclerNewRecipes.apply {
            layoutManager = GridLayoutManager(this@NewRecipesActivity, 2)
            adapter = this@NewRecipesActivity.adapter
        }

        // ViewModel
        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        viewModel.recipes.observe(this) { list ->
            if (list.isEmpty()) {
                Toast.makeText(this, "Chưa có công thức mới nào", Toast.LENGTH_SHORT).show()
            }
            adapter.submitList(list)
        }
        viewModel.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchAllNewRecipes()
    }
}
