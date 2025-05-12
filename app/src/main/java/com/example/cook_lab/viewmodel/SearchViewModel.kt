package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.RecipeResponse
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.repository.SearchRepository
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchViewModel : ViewModel() {

    private val repository = SearchRepository()

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
                    val recipes = response.body()?.data
                    // Nếu tìm thấy công thức, cập nhật LiveData
                    if (recipes.isNullOrEmpty()) {
                        _error.postValue("No recipes found.")
                        _recipes.postValue(emptyList()) // Đặt _recipes rỗng nếu không tìm thấy công thức
                    } else {
                        _recipes.postValue(recipes)
                        Log.e("SearchViewModel", "Recipes: $recipes")
                    }
                } else {
                    // Xử lý khi API không trả về kết quả đúng
                    _error.postValue("No recipes found.")
                    _recipes.postValue(emptyList())
                    Log.e("SearchViewModel", "API Error: ${response.message()}")
                }
            } catch (e: Exception) {
                // Xử lý lỗi kết nối hoặc các lỗi khác
                _error.postValue("Error: ${e.message}")
                _recipes.postValue(emptyList()) // Đặt _recipes rỗng trong trường hợp lỗi
                Log.e("SearchViewModel", "Error: ${e.message}")
            }
        }
    }

}
