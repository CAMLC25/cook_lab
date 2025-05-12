package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.model.Category
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    _categories.postValue(response.body()!!.data)
                } else {
                    _error.postValue("Lỗi: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Lỗi kết nối: ${e.message}")
                Log.e("CategoryVM", "Lỗi: ${e.message}")
            }
        }
    }

    fun fetchRecipesByCategory(categoryId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getRecipesByCategory(categoryId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _recipes.postValue(response.body()!!.data)
                } else {
                    Log.e("CategoryVM", "Lỗi tải công thức: ${response.message()}")
                    _error.postValue("Lỗi tải công thức: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("CategoryVM", "Lỗi: ${e.message}")
                _error.postValue("Lỗi kết nối: ${e.message}")
            }
        }
    }
}
