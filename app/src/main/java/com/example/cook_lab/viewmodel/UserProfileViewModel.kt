package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.BasicResponse
import com.example.cook_lab.data.api.BasicSaveResponse
import com.example.cook_lab.data.api.RecipeResponse
import com.example.cook_lab.data.api.UserProfileResponse
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class UserProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val gson = Gson()

    private val _userProfile = MutableLiveData<UserProfileResponse>()
    val userProfile: LiveData<UserProfileResponse> get() = _userProfile

    private val _savedRecipes = MutableLiveData<List<Recipe>>()
    val savedRecipes: LiveData<List<Recipe>> get() = _savedRecipes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    val deleteRecipeResponse: MutableLiveData<BasicSaveResponse> = MutableLiveData()

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
                val nameRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
                val emailRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email)
                val idCookpadRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), idCookpad)

                val passwordRequestBody = password?.let {
                    RequestBody.create("text/plain".toMediaTypeOrNull(), it)
                }

                val avatarRequestBody = avatarFile?.let {
                    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), it)
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

    // Lấy danh sách công thức đã lưu từ Repository
    fun getSavedRecipes(userId: Int) {
        viewModelScope.launch {
            try {
                val response: Response<RecipeResponse> = userRepository.getSavedRecipes(userId)

                if (response.isSuccessful && response.body()?.success == true) {
                    _savedRecipes.value = response.body()?.data
                    Log.e("UserProfileViewModel", "Saved recipes: ${response.body()?.data}")
                } else {
                    _error.value = "Error: ${response.message()}"
                    Log.e("UserProfileViewModel", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Exception: ${e.message}"
            }
        }
    }

    // Xóa công thức
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                // Gọi API xóa công thức
                val response: Response<BasicSaveResponse> = userRepository.deleteRecipe(recipeId)

                // Kiểm tra phản hồi từ API
                if (response.isSuccessful && response.body() != null) {
                    deleteRecipeResponse.value = response.body() // Thành công, cập nhật response
                    Log.d("UserProfileViewModel", "Xóa công thức thành công.")
                } else {
                    _error.value = "Không thể xóa công thức." // Thất bại, hiển thị lỗi
                }
            } catch (e: Exception) {
                // Nếu có lỗi xảy ra trong quá trình gọi API
                _error.value = "Lỗi: ${e.message}"
                Log.e("EditRecipeViewModel", "Error: ${e.message}") // Ghi lỗi vào log
            }
        }
    }

}