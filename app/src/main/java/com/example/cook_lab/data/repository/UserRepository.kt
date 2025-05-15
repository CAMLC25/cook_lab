package com.example.cook_lab.data.repository

import android.util.Log
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.BasicResponse
import com.example.cook_lab.data.api.BasicSaveResponse
import com.example.cook_lab.data.api.RecipeResponse
import com.example.cook_lab.data.api.UserProfileResponse
import com.example.cook_lab.data.model.MeResponse
import com.example.cook_lab.data.model.Recipe
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class UserRepository {

    suspend fun getUserProfile(userId: Int): Response<UserProfileResponse> {
        return ApiClient.apiService.getUserProfile(userId)
    }

    suspend fun updateUserProfile(
        userId: Int,
        name: RequestBody,
        email: RequestBody,
        idCookpad: RequestBody,
        password: RequestBody?,
        avatar: MultipartBody.Part?
    ): Response<UserProfileResponse> {
        return ApiClient.apiService.updateUserProfile(
            userId,
            name,
            email,
            idCookpad,
            password,
            avatar
        )
    }

    suspend fun getUser(): Response<MeResponse> {
        return ApiClient.apiService.me() // Gọi API me()
    }

    suspend fun getSavedRecipes(userId: Int): Response<RecipeResponse> {
        return ApiClient.apiService.getSavedRecipes(userId)
    }

    suspend fun deleteRecipe(recipeId: Int): Response<BasicSaveResponse> {
        return ApiClient.apiService.deleteRecipe(recipeId)
    }
}
