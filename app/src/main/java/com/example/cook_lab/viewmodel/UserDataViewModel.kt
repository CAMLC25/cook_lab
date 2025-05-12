package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.MeResponse
import com.example.cook_lab.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Response

class UserDataViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val gson = Gson()

    // LiveData để lưu trữ dữ liệu người dùng
    private val _userData = MutableLiveData<MeResponse>()
    val userData: LiveData<MeResponse> get() = _userData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Lấy thông tin người dùng từ API
    fun getUserData() {
        viewModelScope.launch {
            try {
                // Gọi API me() để lấy thông tin người dùng
                val response: Response<MeResponse> = userRepository.getUser()

                if (response.isSuccessful) {
                    response.body()?.let { meResponse ->
                        // Lưu thông tin người dùng vào Prefs
                        val updatedUserJson = gson.toJson(meResponse.user)
                        Prefs.userJson = updatedUserJson

                        // Cập nhật LiveData với dữ liệu người dùng
                        _userData.postValue(meResponse)
                        Log.e("UserDataViewModel", "User data updated successfully: $updatedUserJson")
                    }
                } else {
                    _error.postValue("Error fetching user data: ${response.message()}")
                    Log.e("UserDataViewModel", "Error fetching user data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error fetching user data: ${e.message}")
                Log.e("UserDataViewModel", "Error fetching user data: ${e.message}")
            }
        }
    }
}
