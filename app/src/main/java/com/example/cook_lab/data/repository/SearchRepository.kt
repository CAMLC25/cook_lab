package com.example.cook_lab.data.repository

import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.RecipeResponse

import com.example.cook_lab.data.api.SearchHistoryResponse
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

    // Get search history
    suspend fun getSearchHistory(userId: Int): Response<SearchHistoryResponse> {
        return ApiClient.apiService.getSearchHistory(userId)
    }

    // Delete specific search history
    suspend fun deleteSearchHistory(userId: Int, id: Int): Response<SearchHistoryResponse> {
        return ApiClient.apiService.deleteSearchHistory(userId, id)
    }

    // Xóa tất cả lịch sử tìm kiếm
    suspend fun deleteAllSearchHistory(): Response<Void> {
        return ApiClient.apiService.deleteAllSearchHistory()
    }
}
