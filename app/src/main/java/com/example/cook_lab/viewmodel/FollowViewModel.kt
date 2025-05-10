package com.example.cook_lab.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.cook_lab.data.api.ApiClient
import kotlinx.coroutines.launch

class FollowViewModel : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> get() = _isFollowing

    private val _followersCount = MutableLiveData<Int>()
    val followersCount: LiveData<Int> get() = _followersCount

    private val _followingCount = MutableLiveData<Int>()
    val followingCount: LiveData<Int> get() = _followingCount

    fun checkIfUserFollows(followeeId: Int) {
        viewModelScope.launch {
            try {
                // API request to check follow status
                val response = ApiClient.apiService.checkIfUserFollows(followeeId)

                // Handle the response
                if (response.isSuccessful) {
                    val isFollowing = response.body()?.isFollowing ?: false
                    // Update the LiveData with the result
                    _isFollowing.postValue(isFollowing)
                    Log.d("FollowViewModel", "Check follow status: $isFollowing")
                } else {
                    // If the response is not successful, set to false
                    _isFollowing.postValue(false)
                    Log.e("FollowViewModel", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                // In case of any error, set the follow status to false
                _isFollowing.postValue(false)
                Log.e("FollowViewModel", "Error checking follow status: ${e.localizedMessage}")
            }
        }
    }

    fun followUser(followeeId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.followUser(followeeId)
                if (response.isSuccessful) {
                    _isFollowing.value = true  // Cập nhật trạng thái theo dõi
                    _error.value = "Đã theo dõi người dùng"
                    Log.e("FollowViewModel", "Follow user success")
                } else {
                    _isFollowing.value = false
                    _error.value = "Theo dõi thất bại"
                    Log.e("FollowViewModel", "Follow user error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _isFollowing.value = false
                _error.value = "Lỗi kết nối: ${e.localizedMessage}"
                Log.e("FollowViewModel", "Follow user error: ${e.localizedMessage}")
            }
        }
    }

    fun unfollowUser(followeeId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.unfollowUser(followeeId)
                if (response.isSuccessful) {
                    _isFollowing.value = false
                    _error.value = "Đã hủy theo dõi"
                } else {
                    _isFollowing.value = true
                    _error.value = "Hủy theo dõi thất bại"
                    Log.e("FollowViewModel", "Unfollow user error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _isFollowing.value = true
                _error.value = "Lỗi kết nối: ${e.localizedMessage}"
            }
        }
    }

    fun getFollowStats(userId: Int) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getFollowStats(userId)
                if (response.isSuccessful) {
                    _followersCount.value = response.body()?.followersCount ?: 0
                    _followingCount.value = response.body()?.followingCount ?: 0
                } else {
                    _followersCount.value = 0
                    _followingCount.value = 0
                    _error.value = "Không thể tải thông tin follow"
                }
            } catch (e: Exception) {
                _followersCount.value = 0
                _followingCount.value = 0
                _error.value = "Lỗi kết nối: ${e.localizedMessage}"
            }
        }
    }
}
