package com.example.cook_lab.data.api

import com.example.cook_lab.data.model.Category
import com.example.cook_lab.data.model.Comment
import com.example.cook_lab.data.model.LoginRequest
import com.example.cook_lab.data.model.LoginResponse
import com.example.cook_lab.data.model.MeResponse
import com.example.cook_lab.data.model.Reaction
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/recipes")
    suspend fun getAllRecipes(): Response<RecipeResponse>

    @GET("api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") recipeId: Int): Response<RecipeDetailResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @GET("api/categories/{id}/recipes")
    suspend fun getRecipesByCategory(
        @Path("id") categoryId: Int
    ): Response<RecipeResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @FormUrlEncoded
    @POST("api/recipes/{id}/react")
    suspend fun postReaction(
        @Path("id") recipeId: Int,
        @Field("type") type: String
    ): Response<BasicResponse>

    @FormUrlEncoded
    @POST("api/recipes/{id}/comment")
    suspend fun postComment(
        @Path("id") recipeId: Int,
        @Field("content") content: String
    ): Response<CommentResponse>

    @FormUrlEncoded
    @POST("api/recipes/{id}/react/remove")
    suspend fun removeReaction(
        @Path("id") recipeId: Int,
        @Field("type") type: String
    ): Response<BasicResponse>

    @DELETE("api/recipes/{recipeId}/comment/{commentId}")
    suspend fun removeComment(
        @Path("recipeId") recipeId: Int,
        @Path("commentId") commentId: Int
    ): Response<BasicResponse>

    @POST("api/recipes/{id}/save")
    suspend fun saveRecipe(
        @Path("id") recipeId: Int
    ): Response<BasicSaveResponse>

    @DELETE("api/recipes/{id}/unsave")
    suspend fun removeSavedRecipe(
        @Path("id") recipeId: Int
    ): Response<BasicSaveResponse>


    @GET("api/user/check-follow/{recipeOwnerId}")
    suspend fun checkIfUserFollows(
        @Path("recipeOwnerId") recipeOwnerId: Int
    ): Response<FollowStatusResponse>

    @POST("api/user/{followeeId}/follow")
    suspend fun followUser(
        @Path("followeeId") followeeId: Int
    ): Response<FollowResponse>

    @DELETE("api/user/{followeeId}/unfollow")
    suspend fun unfollowUser(
        @Path("followeeId") followeeId: Int
    ): Response<FollowResponse>

    @GET("api/user/{userId}/follow-stats")
    suspend fun getFollowStats(
        @Path("userId") userId: Int
    ): Response<FollowStatsResponse>

    // Tìm kiếm công thức cho khách
    @GET("api/recipes/search/guest")
    suspend fun searchGuestRecipes(@Query("search") search: String): Response<RecipeResponse>

    // Tìm kiếm công thức cho người dùng đã đăng nhập
    @GET("api/recipes/search/auth")
    suspend fun searchAuthRecipes(@Query("search") search: String): Response<RecipeResponse>

}

data class CategoryResponse(
    val success: Boolean,
    val data: List<Category>
)

data class RecipeResponse(
    val success: Boolean,
    val data: List<Recipe>,
)

data class RecipeDetailResponse(
    val success: Boolean,
    val data: RecipeData,
    val message: String?,
)

data class BasicResponse(
    val success: Boolean,
    val data: Reaction,
    val message: String?
)

data class CommentResponse(
    val success: Boolean,
    val data: Comment,
    val message: String?
)

data class RecipeData(
    val recipe: Recipe
)

data class BasicSaveResponse(
    val success: Boolean,
    val message: String
)

data class FollowStatusResponse(
    val isFollowing: Boolean
)

data class FollowResponse(
    val success: Boolean,
    val message: String
)

data class FollowStatsResponse(
    val followersCount: Int,
    val followingCount: Int
)


