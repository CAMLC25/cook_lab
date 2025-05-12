package com.example.cook_lab.data.model

data class User(
    val id: Int,
    var id_cooklab: String,
    var name: String,
    var email: String,
    val avatar: String?,
    val role: String,
    val email_verified_at: String?,
    val status: String,
    val pivot: Pivot?,
    val followers_count: Int,
    val following_count: Int
)

data class Pivot(
    val recipe_id: Int,
    val user_id: Int
)
