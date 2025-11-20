package com.example.cook_lab.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val text: String
)

data class Constraints(
    val avoid: List<String> = emptyList(),
    val diet: String = "none",
    @SerializedName("time_max_min") val timeMaxMin: Int? = null,
    val tools: List<String> = emptyList()
)

data class AiSuggestion(
    val title: String,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    @SerializedName("time_min") val timeMin: Int? = null
)

data class CatalogFilters(
    val ingredients: List<String> = emptyList(),
    val avoid: List<String> = emptyList(),
    val diet: String = "none",
    val intent: String = "recipes",
    @SerializedName("time_max_min") val timeMaxMin: Int? = null
)

data class CatalogQuery(
    @SerializedName("need_catalog") val needCatalog: Boolean = false,
    val filters: CatalogFilters = CatalogFilters()
)

data class CatalogHit(
    val id: String,
    val title: String,
    val image: String? = null,
    val thumb: String? = null,
    @SerializedName("cook_time") val cookTime: String?, // chuỗi "30 phút"
    @SerializedName("time_min") val timeMin: Int?
)

data class AiRecipe(
    val title: String,
    val servings: Int? = null,
    @SerializedName("time_min") val timeMin: Int? = null,
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val tips: List<String> = emptyList()
)

data class ChatResponse(
    val intent: String = "recipes",
    val ingredients: List<String> = emptyList(),
    val constraints: Constraints = Constraints(),
    @SerializedName("ai_suggestions") val aiSuggestions: List<AiSuggestion> = emptyList(),
    @SerializedName("catalog_query") val catalogQuery: CatalogQuery = CatalogQuery(),
    @SerializedName("catalog_hits") val catalogHits: List<CatalogHit> = emptyList(),
    @SerializedName("ai_recipe") val aiRecipe: AiRecipe? = null,
    @SerializedName("dish_names") val dishNames: List<String> = emptyList(),
    val note: String? = null
)
