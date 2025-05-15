package com.example.cook_lab

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
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
import com.example.cook_lab.ui.components.LoginPromptDialog
import com.example.cook_lab.ui.components.RecipeAdapter
import com.example.cook_lab.ui.profile.UserProfileActivity
import com.example.cook_lab.ui.recipe.CreateRecipeActivity
import com.example.cook_lab.ui.recipe.NewRecipesActivity
import com.example.cook_lab.ui.recipe.RecipeDetailActivity
import com.example.cook_lab.ui.recipe.SearchHistoryActivity
import com.example.cook_lab.ui.recipe.SearchResultsActivity
import com.example.cook_lab.viewmodel.CategoryViewModel
import com.example.cook_lab.viewmodel.RecipeViewModel
import com.example.cook_lab.viewmodel.UserDataViewModel
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn = false
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var viewModel: UserDataViewModel
    private val gson = Gson()

    companion object {
        const val REQUEST_USER_PROFILE = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initializeActivity()
    }

    private fun initializeActivity() {
        // Determine login state
        isLoggedIn = !Prefs.token.isNullOrEmpty() && Prefs.userJson != null
        Log.e("MainActivity", "isLoggedIn: $isLoggedIn")

        // Hiển thị hoặc ẩn addRecipeButton dựa trên trạng thái đăng nhập
//        binding.addRecipeButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
        binding.addRecipeButton.visibility = View.VISIBLE

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)
        // Lấy thông tin người dùng từ API và cập nhật vào Prefs
        if (isLoggedIn) {
            viewModel.getUserData()
            Log.e("MainActivity", "getUserData() called")
        }

        // Observe LiveData từ UserDataViewModel chỉ khi đã đăng nhập
        viewModel.userData.observe(this) { meResponse ->
            meResponse?.let { response ->
                val user = response.user

                // Cập nhật ảnh đại diện và thông tin người dùng trong drawer
                Glide.with(this)
                    .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(binding.drawerIcon)

                val navView: NavigationView = binding.navView
                val header = navView.getHeaderView(0)
                val imageAvatar = header.findViewById<ImageView>(R.id.imageAvatar)
                val textName = header.findViewById<TextView>(R.id.textName)
                val textUsername = header.findViewById<TextView>(R.id.textUsername)

                textName.text = "Xin chào ${user.name}"
                textUsername.text = "ID: @${user.id_cooklab}"

                Glide.with(this)
                    .load("${ApiClient.BASE_URL.removeSuffix("/")}/${user.avatar}")
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(imageAvatar)

                // Replace menu icon with user avatar when logged in
                if (isLoggedIn && Prefs.userJson != null) {
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

                // Ẩn ProgressBar khi dữ liệu được tải
                binding.loadingProgressBar.visibility = View.GONE
            }
        }

        // Kiểm tra lỗi nếu có
        viewModel.error.observe(this) { error ->
            Log.e("MainActivity", "Error: $error")
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            // Ẩn ProgressBar nếu có lỗi
            binding.loadingProgressBar.visibility = View.GONE
        }

        setupListeners()
        setupDrawer()
        setupCategoryList()
        setupNewRecipesList()
    }

    private fun setupListeners() {
        // Drawer toggle
        binding.drawerIcon.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.notificationIcon.setOnClickListener {
            if (!requireLogin()) return@setOnClickListener
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
            if (!requireLogin()) return@setOnClickListener
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    resetToInitialState()
                    Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_library -> {
                    if (!requireLogin()) return@setOnItemSelectedListener false
                    Prefs.userJson?.let { json ->
                        val user = gson.fromJson(json, User::class.java)
                        val userId: Int? = user.id
                        Log.e("MainActivity_kho", "userId: $userId")
                        val intent = Intent(this, KhoActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)
                    }
                    true
                }

                else -> false
            }
        }
        // Danh sách công thức mới
        binding.newRecipesArrow.setOnClickListener {
            startActivity(Intent(this, NewRecipesActivity::class.java))
        }
    }

    private fun resetToInitialState() {
        // Hiển thị ProgressBar
        binding.loadingProgressBar.visibility = View.VISIBLE

        // Clear search input
        binding.searchInputLayout.editText?.setText("")

        // Reset drawer state
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Trì hoãn 2 giây để hiển thị hiệu ứng loading
        Handler(Looper.getMainLooper()).postDelayed({
            // Reinitialize data and UI
            if (isLoggedIn) {
                viewModel.getUserData() // Refresh user data
            }
            setupCategoryList() // Reset category list
            setupNewRecipesList() // Reset new recipes list

            // Scroll RecyclerViews to top
            binding.categoriesRecyclerView.scrollToPosition(0)
            binding.newRecipesRecyclerView.scrollToPosition(0)

            // Ẩn ProgressBar sau khi reset
            binding.loadingProgressBar.visibility = View.GONE
        }, 2000) // Trì hoãn 2000ms (2 giây)
    }

    private fun setupDrawer() {
        val navView: NavigationView = binding.navView
        val header = navView.getHeaderView(0)
        val headerLayout = header.findViewById<LinearLayout>(R.id.nav_header_layout)
        val imageAvatar = header.findViewById<ImageView>(R.id.imageAvatar)
        val textName = header.findViewById<TextView>(R.id.textName)
        val textUsername = header.findViewById<TextView>(R.id.textUsername)

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
                    if (!requireLogin()) return@setNavigationItemSelectedListener true
                    Toast.makeText(this, "Hồ sơ", Toast.LENGTH_SHORT).show()
                    Prefs.userJson?.let { json ->
                        val user = gson.fromJson(json, User::class.java)
                        val userId: Int? = user.id
                        val intent = Intent(this, UserProfileActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        Log.e("MainActivity", "userId: $userId")
                        startActivityForResult(intent, REQUEST_USER_PROFILE)
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_recently_viewed -> {
                    if (!requireLogin()) return@setNavigationItemSelectedListener true
                    Toast.makeText(this, "Lịch sử tìm kiếm.", Toast.LENGTH_SHORT).show()
                    Prefs.userJson?.let { json ->
                        val user = gson.fromJson(json, User::class.java)
                        val userId: Int? = user.id
                        val intent = Intent(this, SearchHistoryActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        Log.e("MainActivity", "userId: $userId")
                        startActivity(intent)
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    }
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
        categoryViewModel.categories.observe(this) { list ->
            categoryAdapter.setData(list)
            // Ẩn ProgressBar khi dữ liệu danh mục được tải
            binding.loadingProgressBar.visibility = View.GONE
        }
        categoryViewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            // Ẩn ProgressBar nếu có lỗi
            binding.loadingProgressBar.visibility = View.GONE
        }
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
        recipeViewModel.recipes.observe(this) { list ->
            recipeAdapter.setData(list)
            // Ẩn ProgressBar khi dữ liệu công thức được tải
            binding.loadingProgressBar.visibility = View.GONE
        }
        recipeViewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            // Ẩn ProgressBar nếu có lỗi
            binding.loadingProgressBar.visibility = View.GONE
        }
    }

    private fun searchRecipes(query: String) {
        val intent = Intent(this, SearchResultsActivity::class.java)
        intent.putExtra("searchQuery", query)
        Log.e("searchRecipes", "query: $query")
        intent.putExtra("isLoggedIn", isLoggedIn)
        Log.e("searchRecipes", "isLoggedIn: $isLoggedIn")
        startActivity(intent)
    }

    private fun requireLogin(): Boolean {
        if (!isLoggedIn || Prefs.userJson == null) {
            LoginPromptDialog(this).show()
            Log.d("MainActivity", "Action blocked: user not logged in")
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USER_PROFILE && resultCode == Activity.RESULT_OK) {
            val resetRequired = data?.getBooleanExtra("RESET_REQUIRED", false) ?: false
            if (resetRequired) {
                resetToInitialState()
                Toast.makeText(this, "Đã đến trang chủ", Toast.LENGTH_SHORT).show()
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
}