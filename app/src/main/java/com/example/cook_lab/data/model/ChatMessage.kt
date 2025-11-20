package com.example.cook_lab.data.model

data class ChatMessage(
    val id: String,
    val role: Role,        // USER | BOT
    val type: Type,        // TEXT | IMAGE_CARD | DETECTED_CHIPS | RECIPE_LIST
    val text: String? = null,
    val imageUrl: String? = null,
    val chips: List<String> = emptyList(),
    val recipes: List<RecipeCard> = emptyList(),
)

enum class Role { USER, BOT }
enum class Type { TEXT, IMAGE_CARD, DETECTED_CHIPS, RECIPE_LIST }

data class RecipeCard(
    val title: String,
    val time: String,
    val kcal: Int,
    val tags: List<String>,
    val imageUrl: String
)
