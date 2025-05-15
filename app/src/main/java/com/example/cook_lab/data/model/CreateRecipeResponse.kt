package com.example.cook_lab.data.model

data class CreateRecipeResponse(
    val success: Boolean,
    val message: String,
    val data: CreateRecipe // Đây là công thức vừa được tạo
)

data class CreateRecipeRequest(
    val title: String,                        // Tiêu đề của công thức
    val description: String?,                 // Mô tả công thức (có thể là null)
    val image: String?,                       // Đường dẫn ảnh chính (có thể là null)
    val category_id: Int,                     // ID danh mục
    val cook_time: String,                    // Thời gian nấu
    val servings: String,                     // Số lượng khẩu phần
    val ingredients: List<String>,            // Danh sách nguyên liệu
    val steps: List<StepRequest>              // Danh sách các bước làm
)


data class CreateRecipe(
    val id: Int,
    val title: String,
    val description: String?,
    val image: String?,
    val categoryId: Int,
    val cookTime: String,
    val servings: String,
    val ingredients: List<String>,
    val steps: List<StepRequest>
)

data class StepRequest(
    val description: String,
    val image: String? // Ảnh của bước
)