package com.example.cook_lab.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cook_lab.databinding.ActivitySearchHistoryBinding
import com.example.cook_lab.ui.BaseActivity
import com.example.cook_lab.ui.components.SearchHistoryAdapter
import com.example.cook_lab.viewmodel.SearchHistoryViewModel

class SearchHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchHistoryBinding
    private lateinit var adapter: SearchHistoryAdapter
    private val searchHistoryViewModel: SearchHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar with "Back" button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId != -1) {
            searchHistoryViewModel.getSearchHistory(userId)
        } else {
            Log.e("SearchHistoryActivity", "Invalid User ID")
            Toast.makeText(this, "Lỗi: Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
        }

        adapter = SearchHistoryAdapter(
            onDeleteClick = { searchHistory ->
                searchHistoryViewModel.deleteSearchHistory(userId, searchHistory.id)
            },
            onItemClick = { searchHistory ->
                val intent = Intent(this, SearchResultsActivity::class.java)
                intent.putExtra("searchQuery", searchHistory.keyword)
                startActivity(intent)
            }
        )

        adapter = SearchHistoryAdapter(
            onDeleteClick = { history ->
                // Xóa mục ngay trong UI
                adapter.removeItem(history)
                // Sau đó gọi xóa từ backend và refresh dữ liệu
                searchHistoryViewModel.deleteSearchHistory(userId, history.id)
            },
            onItemClick = { history ->
                startActivity(Intent(this, SearchResultsActivity::class.java).apply {
                    putExtra("searchQuery", history.keyword)
                })
            }
        )

        binding.searchHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryRecyclerView.adapter = adapter

        binding.addRecipeButton.setOnClickListener {
            if (!requireLogin()) return@setOnClickListener
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            Toast.makeText(this, "Mở màn hình tạo công thức", Toast.LENGTH_SHORT).show()
        }

        // Observe the search history data
        searchHistoryViewModel.searchHistory.observe(this) { historyList ->
            adapter.setData(historyList ?: emptyList())
        }

        // Observe the delete status
//        searchHistoryViewModel.success.observe(this) { success ->
//            if (success) recreate()
//        }

        // Observe errors
        searchHistoryViewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })

        // Fetch search history when activity is created
        searchHistoryViewModel.getSearchHistory(userId)
    }
    // Override this method to handle back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Handle back button press (go back to previous screen)
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}



