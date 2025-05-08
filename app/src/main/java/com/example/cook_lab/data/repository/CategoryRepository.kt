package com.example.cook_lab.data.repository

import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.CategoryResponse
import com.example.cook_lab.data.api.RecipeResponse
import retrofit2.Response

class CategoryRepository {
    suspend fun getCategories(): Response<CategoryResponse> {
        return ApiClient.apiService.getCategories()
    }

    suspend fun getRecipesByCategory(categoryId: Int): Response<RecipeResponse> {
        return ApiClient.apiService.getRecipesByCategory(categoryId)
    }

}

