package com.example.cook_lab.repository

import com.example.cook_lab.data.api.ApiService
import com.example.cook_lab.data.model.CreateRecipeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CreateRecipeRepository(private val apiService: ApiService) {

    suspend fun createRecipe(
        title: RequestBody,
        description: RequestBody?,
        categoryId: RequestBody,
        cookTime: RequestBody,
        servings: RequestBody,
        ingredients: List<RequestBody>,
        stepDescriptions: List<RequestBody>,
        stepImages: List<MultipartBody.Part>,
        image: MultipartBody.Part // Ảnh chính của công thức
    ): CreateRecipeResponse {
        // Gọi API tạo công thức
        val response = apiService.createRecipe(
            title, description, categoryId, cookTime, servings,
            ingredients, stepDescriptions, stepImages, image
        )

        // Kiểm tra phản hồi từ API
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Lỗi: Không có dữ liệu từ server")
        } else {
            throw Exception("Lỗi: ${response.message()}")
        }
    }

    // Phương thức để cập nhật công thức
    suspend fun updateRecipe(
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
    ): CreateRecipeResponse {
        // Gọi API cập nhật công thức
        val response = apiService.updateRecipe(
            recipeId, title, description, categoryId, cookTime, servings,
            ingredients, stepDescriptions, stepImages, image
        )

        // Kiểm tra phản hồi từ API
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Lỗi: Không có dữ liệu từ server")
        } else {
            throw Exception("Lỗi: ${response.message()}")
        }
    }
}
