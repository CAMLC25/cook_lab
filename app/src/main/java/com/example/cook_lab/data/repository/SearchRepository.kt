package com.example.cook_lab.data.repository

import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.RecipeResponse
import retrofit2.Response

class SearchRepository {

    // Phương thức tìm kiếm công thức cho người dùng đã đăng nhập
    suspend fun searchAuthRecipes(query: String): Response<RecipeResponse> {
        return ApiClient.apiService.searchAuthRecipes(query)
    }

    // Phương thức tìm kiếm công thức cho người dùng chưa đăng nhập
    suspend fun searchGuestRecipes(query: String): Response<RecipeResponse> {
        return ApiClient.apiService.searchGuestRecipes(query)
    }
}
