package com.example.cook_lab.model

data class Recipe(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val searchTime: String? = null // Thời gian tìm kiếm (cho danh sách gần đây).
)
