package com.example.cook_lab.data.repository

import android.util.Log
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.CustomerProfileResponse
import retrofit2.Response


class CustomerRepository {
    suspend fun getCustomerProfile(userId: Int): Response<CustomerProfileResponse> {
        return ApiClient.apiService.getCustomerProfile(userId)
        Log.e("CustomerRepository", "Fetching customer profile for user ID: $userId")
    }
}
