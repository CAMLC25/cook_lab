package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cook_lab.data.api.ApiClient.apiService
import com.example.cook_lab.data.api.RecipeDetailResponse
import com.example.cook_lab.data.model.CreateRecipeResponse
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.repository.CreateRecipeRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class EditRecipeViewModel(private val repository: CreateRecipeRepository) : ViewModel() {

    val recipe: MutableLiveData<Recipe> = MutableLiveData()
    val updateRecipeResponse: MutableLiveData<CreateRecipeResponse> = MutableLiveData()
    val error: MutableLiveData<String> = MutableLiveData()

    // Cập nhật công thức
    fun updateRecipe(
        recipeId: Int,
        title: RequestBody,
        description: RequestBody?,
        categoryId: RequestBody,
        cookTime: RequestBody,
        servings: RequestBody,
        ingredients: List<RequestBody>,
        stepDescriptions: List<RequestBody>,
        stepImages: List<MultipartBody.Part>,
        image: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateRecipe(
                    recipeId, title, description, categoryId, cookTime, servings,
                    ingredients, stepDescriptions, stepImages, image
                )
                updateRecipeResponse.value = response
            } catch (e: Exception) {
                error.value = e.message
                Log.e("CreateRecipeViewModel", "Error: ${e.message}")
            }
        }
    }

    // Phương thức để lấy thông tin công thức theo ID
    fun getRecipeById(recipeId: Int) {
        viewModelScope.launch {
            try {
                val response: Response<RecipeDetailResponse> = apiService.getRecipeById(recipeId)
                if (response.isSuccessful && response.body() != null) {
                    recipe.value = response.body()?.data?.recipe // Lưu công thức vào LiveData
                } else {
                    error.value = "Lỗi: Không tìm thấy công thức."
                }
            } catch (e: Exception) {
                error.value = "Lỗi kết nối: ${e.message}"
                Log.e("EditRecipeViewModel", "Error: ${e.message}")
            }
        }
    }
}
