package com.example.cook_lab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.User
import com.example.cook_lab.databinding.ActivityMainBinding
import com.example.cook_lab.ui.StartActivity
import com.example.cook_lab.ui.auth.LoginActivity
import com.example.cook_lab.ui.category.CategoryDetailActivity
import com.example.cook_lab.ui.components.CategoryAdapter
import com.example.cook_lab.ui.components.RecipeAdapter
import com.example.cook_lab.ui.recipe.NewRecipesActivity
import com.example.cook_lab.ui.recipe.RecipeDetailActivity
import com.example.cook_lab.ui.recipe.SearchResultsActivity
import com.example.cook_lab.viewmodel.CategoryViewModel
import com.example.cook_lab.viewmodel.RecipeViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn = false
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryViewModel: CategoryViewModel

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Determine login state
        isLoggedIn = !Prefs.token.isNullOrEmpty()
        Log.e("MainActivity", "isLoggedIn: $isLoggedIn")

        // Replace menu icon with user avatar when logged in
        if (isLoggedIn && Prefs.userJson != null) {
            val user = gson.fromJson(Prefs.userJson, User::class.java)
            Glide.with(this)
                .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
                .placeholder(R.drawable.account)
                .circleCrop()
                .into(binding.drawerIcon)
            // Remove any tint so avatar shows correctly
            binding.drawerIcon.clearColorFilter()
            binding.drawerIcon.imageTintList = null
        } else {
            binding.drawerIcon.setImageResource(R.drawable.start_activity)
            val white = ContextCompat.getColor(this, android.R.color.white)
            binding.drawerIcon.setColorFilter(white)
        }

        // Drawer toggle
        binding.drawerIcon.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.notificationIcon.setOnClickListener {
            Toast.makeText(this, "Mở màn hình thông báo", Toast.LENGTH_SHORT).show()
        }
        // Search action
        binding.searchInputLayout.editText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_SEARCH || event?.action == KeyEvent.ACTION_DOWN) {
                val q = v.text.toString()
                searchRecipes(q)
                Toast.makeText(this, "Tìm kiếm: $q", Toast.LENGTH_SHORT).show()
                true
            } else false
        }
        binding.addRecipeButton.setOnClickListener {
            Toast.makeText(this, "Thêm công thức mới", Toast.LENGTH_SHORT).show()
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, KhoActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Danh sách công thức mới
        binding.newRecipesArrow.setOnClickListener {
            startActivity(Intent(this, NewRecipesActivity::class.java))
        }

        setupDrawer()
        setupCategoryList()
        setupNewRecipesList()
    }

    private fun setupDrawer() {
        val navView: NavigationView = binding.navView
        val header = navView.getHeaderView(0)
        val headerLayout = header.findViewById<LinearLayout>(R.id.nav_header_layout)
        val imageAvatar = header.findViewById<ImageView>(R.id.imageAvatar)
        val textName = header.findViewById<TextView>(R.id.textName)
        val textUsername = header.findViewById<TextView>(R.id.textUsername)

        // Bind user info if logged in
        Prefs.userJson?.let { json ->
            val user = gson.fromJson(json, User::class.java)
            textName.text = "Xin chào ${user.name}"
            textUsername.text = "ID: @${user.id_cooklab}"
            Glide.with(this)
                .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
                .placeholder(R.drawable.account)
                .circleCrop()
                .into(imageAvatar)
        }

        headerLayout.setOnClickListener {
            if (!isLoggedIn) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                Toast.makeText(this, "Bạn đã đăng nhập", Toast.LENGTH_SHORT).show()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Show logout only when logged in
        navView.menu.findItem(R.id.nav_logout).isVisible = isLoggedIn
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(this, "Hồ sơ", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_recently_viewed -> {
                    Toast.makeText(this, "Món vừa xem", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        try { ApiClient.apiService.logout() } catch (_: Exception) { }
                        Prefs.clear()
                        startActivity(Intent(this@MainActivity, StartActivity::class.java))
                        finishAffinity()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupCategoryList() {
        categoryAdapter = CategoryAdapter { category ->
            startActivity(Intent(this, CategoryDetailActivity::class.java).apply {
                putExtra("CATEGORY_ID", category.id)
                putExtra("CATEGORY_TITLE", category.name)
            })
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        categoryViewModel.categories.observe(this) { list -> categoryAdapter.setData(list) }
        categoryViewModel.error.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun setupNewRecipesList() {
        val recipeAdapter = RecipeAdapter { recipe ->
        startActivity(Intent(this, RecipeDetailActivity::class.java).apply {
            putExtra("RECIPE_ID", recipe.id)
        })
    }
        binding.newRecipesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recipeAdapter
        }
        val recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]
        recipeViewModel.recipes.observe(this) { list -> recipeAdapter.setData(list) }
        recipeViewModel.error.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun searchRecipes(query: String) {
        val intent = Intent(this, SearchResultsActivity::class.java)
        intent.putExtra("searchQuery", query)
        Log.e("searchRecipes", "query: $query")
        intent.putExtra("isLoggedIn", isLoggedIn)
        Log.e("searchRecipes", "isLoggedIn: $isLoggedIn")
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}