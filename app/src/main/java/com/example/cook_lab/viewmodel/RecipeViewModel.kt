package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel: ViewModel() {
    private val repository = RecipeRepository()

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        fetchAllRecipes()
    }

    private fun fetchAllRecipes() {
        viewModelScope.launch {
            try {
                val response = repository.getAllRecipes()
                if (response.isSuccessful && response.body()?.success == true) {
                    _recipes.postValue(response.body()!!.data)
                } else {
                    _error.postValue("Lỗi: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Lỗi kết nối: ${e.message}")
                Log.e("RecipeViewModel", "Error: ${e.message}")
            }
        }
    }

     fun fetchAllNewRecipes() {
        viewModelScope.launch {
            try {
                val response = repository.getAllRecipes()
                if (response.isSuccessful && response.body()?.success == true) {
                    _recipes.postValue(response.body()!!.data)
                } else {
                    _error.postValue("Lỗi: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Lỗi kết nối: ${e.message}")
                Log.e("RecipeViewModel", "Error: ${e.message}")
            }
        }
    }

}
