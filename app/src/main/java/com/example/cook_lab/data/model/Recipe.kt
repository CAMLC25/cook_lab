package com.example.cook_lab.data.model

data class Recipe(
    val id: Int,
    val title: String,
    val description: String?,
    val image: String?,
    val category_id: Int,
    val user_id: Int,
    val user: User,
    val category: Category,
    val cook_time: String?,
    val servings: String?,
    val status: String,
    val reason_rejected: String?,
    val created_at: String,
    val updated_at: String,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val reactions: List<Reaction>,
    val comments: List<Comment>,
    val saved_by_users: List<User>
)

data class Ingredient(
    val id: Int,
    val recipe_id: Int,
    val name: String
)

data class Step(
    val id: Int,
    val recipe_id: Int,
    val step_number: Int,
    val description: String,
    val image: String?
)

data class Reaction(
    val id: Int,
    val user_id: Int,
    val recipe_id: Int,
    val type: String
)

data class Comment(
    val id: Int,
    val user_id: Int,
    val content: String,
    val created_at: String,
    val user: User
)

