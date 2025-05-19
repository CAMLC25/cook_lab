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

class CreateRecipeViewModel(private val repository: CreateRecipeRepository) : ViewModel() {

    val createRecipeResponse: MutableLiveData<CreateRecipeResponse> = MutableLiveData()
    val error: MutableLiveData<String> = MutableLiveData()

    fun createRecipe(
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
                val response = repository.createRecipe(
                    title, description, categoryId, cookTime, servings,
                    ingredients, stepDescriptions, stepImages, image
                )
                createRecipeResponse.value = response
            } catch (e: Exception) {
                error.value = e.message
                Log.e("CreateRecipeViewModel", "Error: ${e.message}")
            }
        }
    }
}

