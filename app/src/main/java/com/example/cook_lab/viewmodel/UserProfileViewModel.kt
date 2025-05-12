package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.UserProfileResponse
import com.example.cook_lab.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UserProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val gson = Gson()

    private val _userProfile = MutableLiveData<UserProfileResponse>()
    val userProfile: LiveData<UserProfileResponse> get() = _userProfile

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getUserProfile(userId: Int) {
        viewModelScope.launch {
            try {
                val response = userRepository.getUserProfile(userId)
                if (response.isSuccessful) {
                    _userProfile.postValue(response.body())
                } else {
                    _error.postValue("Failed to fetch user profile.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun updateUserProfile(
        userId: Int,
        name: String,
        idCookpad: String,
        email: String,
        password: String?,
        avatarFile: File?
    ) {
        viewModelScope.launch {
            try {
                val nameRequestBody = RequestBody.create(MediaType.parse("text/plain"), name)
                val emailRequestBody = RequestBody.create(MediaType.parse("text/plain"), email)
                val idCookpadRequestBody = RequestBody.create(MediaType.parse("text/plain"), idCookpad)

                val passwordRequestBody = password?.let {
                    RequestBody.create(MediaType.parse("text/plain"), it)
                }

                val avatarRequestBody = avatarFile?.let {
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), it)
                    MultipartBody.Part.createFormData("avatar", it.name, requestBody)
                }

                val response = userRepository.updateUserProfile(
                    userId,
                    nameRequestBody,
                    emailRequestBody,
                    idCookpadRequestBody,
                    passwordRequestBody,
                    avatarRequestBody
                )

                if (response.isSuccessful) {
                    _userProfile.postValue(response.body())
                    Log.e("UserProfileViewModel1", "User profile updated: ${response.body()}")
                } else {
                    _error.postValue("Failed to update user profile")
                    Log.e("UserProfileViewModel2", "Failed to update user profile: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
                Log.e("UserProfileViewModel3", "Error: ${e.message}")
            }
        }
    }
}


