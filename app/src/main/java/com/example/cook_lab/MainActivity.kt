package com.example.cook_lab

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cook_lab.databinding.ActivityMainBinding
import com.example.cook_lab.model.Recipe
import com.example.cook_lab.ui.PopularRecipeAdapter
import com.example.cook_lab.ui.RecentRecipeAdapter
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val popularRecipes by lazy {
        listOf(
            Recipe(name = "Món mặn"),
            Recipe(name = "Canh"),
            Recipe(name = "Canh chua"),
            Recipe(name = "Ức gà áp chảo"),
            Recipe(name = "Chân giò"),
            Recipe(name = "Bắp cải"),
            Recipe(name = "Cà tím"),
            Recipe(name = "Cá ngừ")
        )
    }

    private val recentRecipes by lazy {
        listOf(
            Recipe(name = "Canh", searchTime = "Cách đây 3 ngày"),
            Recipe(name = "Món mặn", searchTime = "Cách đây 3 ngày")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false) // Tắt biểu tượng điều hướng mặc định

        // Thiết lập sự kiện nhấn cho logo để mở Drawer
        binding.drawerIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Thiết lập sự kiện nhấn cho icon thông báo
        binding.notificationIcon.setOnClickListener {
            Toast.makeText(this, "Mở màn hình thông báo", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình thông báo hoặc xử lý thông báo tại đây
        }

        // Thiết lập sự kiện nhấn cho ô tìm kiếm (khi người dùng nhấn Enter hoặc tìm kiếm)
        binding.searchInputLayout.editText?.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchInputLayout.editText?.text.toString()
            Toast.makeText(this, "Tìm kiếm: $query", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic tìm kiếm tại đây (VD: gọi API, lọc danh sách công thức, v.v.)
            true
        }

        // Thiết lập sự kiện nhấn cho Floating Action Button
        binding.addRecipeButton.setOnClickListener {
            Toast.makeText(this, "Thêm công thức mới", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình thêm công thức tại đây
        }

        // Thiết lập sự kiện chọn mục trong Bottom Navigation Bar
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    Toast.makeText(this, "Đã chọn Tìm kiếm", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic cho tab Tìm kiếm tại đây
                    true
                }
                R.id.nav_library -> {
                    Toast.makeText(this, "Đã chọn Kho món ngon", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic cho tab Kho món ngon tại đây
                    true
                }
                else -> false
            }
        }

        // Thiết lập Navigation Drawer
        setupDrawer()

        // Thiết lập RecyclerView cho danh sách phổ biến
        binding.popularRecipesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.popularRecipesRecyclerView.adapter = PopularRecipeAdapter(popularRecipes) { recipe ->
            Toast.makeText(this, "Đã chọn: ${recipe.name}", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình chi tiết công thức tại đây
        }

        // Thiết lập RecyclerView cho danh sách gần đây
        binding.recentRecipesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentRecipesRecyclerView.adapter = RecentRecipeAdapter(recentRecipes) { recipe ->
            Toast.makeText(this, "Đã chọn: ${recipe.name}", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở màn hình chi tiết công thức tại đây
        }

        // Thiết lập sự kiện nhấn cho tiêu đề "Tìm kiếm gần đây" (nếu cần mở rộng danh sách)
        binding.recentRecipesTitle.setOnClickListener {
            Toast.makeText(this, "Mở rộng danh sách tìm kiếm gần đây", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic mở rộng danh sách tìm kiếm gần đây tại đây
        }

        // Thiết lập sự kiện nhấn cho mũi tên bên cạnh "Tìm kiếm gần đây"
        binding.recentRecipesArrow.setOnClickListener {
            Toast.makeText(this, "Xem thêm tìm kiếm gần đây", Toast.LENGTH_SHORT).show()
            // TODO: Bổ sung logic xem thêm danh sách tìm kiếm gần đây tại đây
        }
    }

    private fun setupDrawer() {
        val navView: NavigationView = binding.navView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_recently_viewed -> {
                    Toast.makeText(this, "Món vừa xem", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic mở màn hình "Món vừa xem" tại đây
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_premium -> {
                    Toast.makeText(this, "Premium", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic mở màn hình "Premium" tại đây
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic mở màn hình "Cài đặt" tại đây
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_faq -> {
                    Toast.makeText(this, "Câu hỏi thường gặp", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic mở màn hình "Câu hỏi thường gặp" tại đây
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_feedback -> {
                    Toast.makeText(this, "Gửi góp ý", Toast.LENGTH_SHORT).show()
                    // TODO: Bổ sung logic mở màn hình "Gửi góp ý" tại đây
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // test
}