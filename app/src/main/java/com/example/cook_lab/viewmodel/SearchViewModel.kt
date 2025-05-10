package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.RecipeResponse
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.repository.SearchRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository() // Sử dụng SearchRepository

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Tìm kiếm công thức
    fun searchRecipes(query: String, isAuthenticated: Boolean) {
        viewModelScope.launch {
            try {
                // Gọi repository để thực hiện tìm kiếm
                val response: Response<RecipeResponse> = if (isAuthenticated) {
                    repository.searchAuthRecipes(query)
                } else {
                    repository.searchGuestRecipes(query)
                }

                // Kiểm tra nếu API trả về kết quả thành công
                if (response.isSuccessful && response.body()?.success == true) {
                    _recipes.postValue(response.body()?.data)
                    Log.e("SearchViewModel", "Recipes: ${response.body()?.data}")
                } else {
                    _error.postValue("No recipes found.")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                Log.e("SearchViewModel", "Error: ${e.message}")
            }
        }
    }
}
