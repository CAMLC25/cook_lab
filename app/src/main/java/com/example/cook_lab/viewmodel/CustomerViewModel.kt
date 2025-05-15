package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cook_lab.data.api.CustomerProfileResponse
import com.example.cook_lab.data.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel : ViewModel() {

    private val customerRepository = CustomerRepository()

    private val _customerProfile = MutableLiveData<CustomerProfileResponse>()
    val customerProfile: LiveData<CustomerProfileResponse> get() = _customerProfile
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Lấy thông tin người dùng
    fun getCustomerProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val response = customerRepository.getCustomerProfile(userId)
                Log.e("CustomerViewModel", "Response: $response")
                if (response.isSuccessful) {
                    _customerProfile.postValue(response.body())
                    Log.e("CustomerViewModel", "Customer profile fetched successfully")
                } else {
                    _error.postValue("Không thể tải thông tin người dùng")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}
