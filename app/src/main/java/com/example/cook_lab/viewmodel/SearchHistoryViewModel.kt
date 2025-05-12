package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cook_lab.data.api.SearchHistory
import com.example.cook_lab.data.api.SearchHistoryResponse // Import SearchHistoryResponse
import com.example.cook_lab.data.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchHistoryViewModel : ViewModel() {

    private val repository = SearchRepository()

    private val _searchHistory = MutableLiveData<List<SearchHistory>>()
    val searchHistory: LiveData<List<SearchHistory>> = _searchHistory

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    // Get search history
    fun getSearchHistory(userId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getSearchHistory(userId)
                Log.e("SearchHistoryViewModel", "Response: $response")
                if (response.isSuccessful) {
                    // Lấy danh sách search history từ response.body()?.data
                    _searchHistory.postValue(response.body()?.data)
                    Log.e("SearchHistoryViewModel", "Lịch sử tìm kiếm đã được tải thành công")
                } else {
//                    _error.postValue("Failed to load search history")
                    Log.e("SearchHistoryViewModel", "Failed to load search history")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                Log.e("SearchHistoryViewModel", "Error: ${e.message}")
            }
        }
    }

    // Delete specific search history
    fun deleteSearchHistory(userId: Int, id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteSearchHistory(userId, id)
                if (response.isSuccessful) {
                    _success.postValue(true)
                    _error.postValue("Lịch sử tìm kiếm đã được xóa \n            thành công!")
                    getSearchHistory(userId)
                } else {
//                    _error.postValue("Failed to delete search history")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}
