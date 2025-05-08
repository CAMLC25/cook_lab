package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Comment
import com.example.cook_lab.data.model.Recipe
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {
    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe> = _recipe

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _reactionPosted = MutableLiveData<String?>()
    val reactionPosted: LiveData<String?> = _reactionPosted

    private val _commentPosted = MutableLiveData<Comment?>()
    val commentPosted: LiveData<Comment?> = _commentPosted

    fun loadRecipe(id: Int) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.getRecipeById(id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _recipe.value = resp.body()!!.data.recipe
            } else {
                _error.value = resp.body()?.message
                    ?: "Không thể tải công thức (code ${resp.code()})"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
        }
    }

    fun postReaction(recipeId: Int, type: String) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.postReaction(recipeId, type)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _reactionPosted.value = type
                loadRecipe(recipeId)
            } else {
                _error.value = resp.body()?.message ?: "Không gửi được phản ứng"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
        }
    }

    fun postComment(recipeId: Int, content: String) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.postComment(recipeId, content)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _commentPosted.value = resp.body()!!.data
                loadRecipe(recipeId)
            } else {
                _error.value = resp.body()?.message ?: "Không gửi được bình luận"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
        }
    }

    // Phương thức để xóa phản ứng của người dùng
    fun removeReaction(recipeId: Int, type: String) {
        viewModelScope.launch {
            try {
                val resp = ApiClient.apiService.removeReaction(recipeId, type)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    // Xóa phản ứng khỏi UI
                    _reactionPosted.value = null
                    _error.value = "Đã hủy thả biểu tượng cảm xúc này"
                    loadRecipe(recipeId)
                } else {
                    _error.value = resp.body()?.message ?: "Không thể hủy phản ứng"
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.localizedMessage}"
            }
        }
    }

    fun removeComment(recipeId: Int, commentId: Int) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.removeComment(recipeId, commentId)
            if (resp.isSuccessful && resp.body()?.success == true) {
                _commentPosted.value = null
                _error.value = "Bình luận đã được xóa"
                loadRecipe(recipeId)
            } else {
                _error.value = resp.body()?.message ?: "Không thể xóa bình luận"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
        }
    }

    // Lưu công thức
    fun saveRecipe(recipeId: Int) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.saveRecipe(recipeId)
            if (resp.isSuccessful && resp.body()?.success == true) {
//                _recipe.value?.isSaved = true
                loadRecipe(recipeId)
                _error.value = "Công thức đã được lưu thành công."
            } else {
                _error.value = resp.body()?.message ?: "Không thể lưu công thức"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
            Log.e("SaveRecipeError", e.toString())
        }
    }

    // Hủy lưu công thức
    fun removeSavedRecipe(recipeId: Int) = viewModelScope.launch {
        try {
            val resp = ApiClient.apiService.removeSavedRecipe(recipeId)
            if (resp.isSuccessful && resp.body()?.success == true) {
//                _recipe.value?.isSaved = false
                loadRecipe(recipeId)
                _error.value = "Công thức đã bị xóa khỏi danh sách đã lưu."
            } else {
                _error.value = resp.body()?.message ?: "Không thể bỏ lưu công thức"
            }
        } catch (e: Exception) {
            _error.value = "Lỗi kết nối: ${e.localizedMessage}"
        }
    }



}
