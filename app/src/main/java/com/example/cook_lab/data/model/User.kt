package com.example.cook_lab.data.model

data class User(
    val id: Int,
    val id_cooklab: String,
    val name: String,
    val email: String,
    val avatar: String?,
    val role: String,
    val status: String,
    val pivot: Pivot?
)

data class Pivot(
    val recipe_id: Int,
    val user_id: Int
)
