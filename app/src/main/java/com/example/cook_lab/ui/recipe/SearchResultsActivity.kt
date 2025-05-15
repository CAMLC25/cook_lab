package com.example.cook_lab.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cook_lab.databinding.ActivitySearchResultsBinding
import com.example.cook_lab.ui.BaseActivity
import com.example.cook_lab.ui.components.SearchResultsAdapter
import com.example.cook_lab.viewmodel.SearchViewModel

class SearchResultsActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý toolbar với nút back
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back

        // Lấy từ khóa tìm kiếm từ Intent
        val query = intent.getStringExtra("searchQuery") ?: ""
        val isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

        // 2. Đặt lại text cho ô tìm kiếm
        binding.searchInputLayout.editText?.apply {
            setText(query)
            // di chuyển con trỏ về cuối chuỗi
            setSelection(query.length)
        }

        // Khởi tạo RecyclerView Adapter
        searchResultsAdapter = SearchResultsAdapter { recipe ->
            // Khi nhấn vào công thức, chuyển tới màn hình chi tiết
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchResultsActivity)
            adapter = this@SearchResultsActivity.searchResultsAdapter
        }

        // Khởi tạo SearchViewModel
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        searchViewModel.searchRecipes(query, isLoggedIn)

        // Quan sát kết quả tìm kiếm
        searchViewModel.recipes.observe(this, Observer { recipes ->
            // Adapter tự xử lý empty state khi danh sách rỗng
            searchResultsAdapter.setData(recipes ?: emptyList())
        })

        // Quan sát lỗi tìm kiếm
        searchViewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })

        // Xử lý ô tìm kiếm trong màn hình kết quả
        binding.searchInputLayout.editText?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_SEARCH || event?.action == KeyEvent.ACTION_DOWN) {
                val searchQuery = v.text.toString()
                val intent = Intent(this, SearchResultsActivity::class.java)
                intent.putExtra("searchQuery", searchQuery)
                startActivity(intent)
                true
            } else false
        }

        binding.addRecipeButton.setOnClickListener {
            if (!requireLogin()) return@setOnClickListener
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Quay lại màn hình trước
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
