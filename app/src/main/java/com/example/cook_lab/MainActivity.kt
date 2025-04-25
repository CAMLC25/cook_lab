package com.example.cook_lab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cook_lab.databinding.ActivityMainBinding
import com.example.cook_lab.ui.RecipeAdapter
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Navigation Drawer
        setupDrawer()

        // Thiết lập RecyclerView cho công thức phổ biến
        binding.popularRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.popularRecipesRecyclerView.adapter = RecipeAdapter(getDummyRecipes())

        // Thiết lập RecyclerView cho công thức gần đây
        binding.recentRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recentRecipesRecyclerView.adapter = RecipeAdapter(getDummyRecipes())

        // Xử lý sự kiện nút thêm công thức
        binding.addRecipeButton.setOnClickListener {
            // TODO: Chuyển đến màn hình đăng công thức
        }
    }

    private fun setupDrawer() {
        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_recently_viewed -> {
                    // TODO: Xử lý mục "Món vừa xem"
                }
                R.id.nav_premium -> {
                    // TODO: Xử lý mục "Premium"
                }
                R.id.nav_settings -> {
                    // TODO: Xử lý mục "Cài đặt"
                }
                R.id.nav_faq -> {
                    // TODO: Xử lý mục "Câu hỏi thường gặp"
                }
                R.id.nav_feedback -> {
                    // TODO: Xử lý mục "Gửi góp ý"
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Dữ liệu giả để hiển thị giao diện
    private fun getDummyRecipes(): List<Recipe> {
        return listOf(
            Recipe(name = "Món mặn"),
            Recipe(name = "Canh"),
            Recipe(name = "Canh chua"),
            Recipe(name = "Ước gà áp chảo"),
            Recipe(name = "Chân giò"),
            Recipe(name = "Bắp cải"),
            Recipe(name = "Cà tím"),
            Recipe(name = "Cá ngừ")
        )
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

data class Recipe(val name: String) // Model tạm thời