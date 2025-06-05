package com.example.cook_lab.data.repository

import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.RecipeResponse
import retrofit2.Response

class RecipeRepository {
    suspend fun getAllRecipes(): Response<RecipeResponse> {
        return ApiClient.apiService.getAllRecipes()
    }

    suspend fun getTrendingRecipes(): Response<RecipeResponse> {
        return ApiClient.apiService.getTrendingRecipes()
    }
}