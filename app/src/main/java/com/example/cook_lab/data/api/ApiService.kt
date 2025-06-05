package com.example.cook_lab.data.api

import com.example.cook_lab.data.model.Category
import com.example.cook_lab.data.model.Comment
import com.example.cook_lab.data.model.CreateRecipeResponse
import com.example.cook_lab.data.model.LoginRequest
import com.example.cook_lab.data.model.LoginResponse
import com.example.cook_lab.data.model.MeResponse
import com.example.cook_lab.data.model.Reaction
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.data.model.RegisterRequest
import com.example.cook_lab.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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
    suspend fun me(): Response<MeResponse>

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
    suspend fun searchGuestRecipes(
        @Query("search") search: String
    ): Response<RecipeResponse>

    // Tìm kiếm công thức cho người dùng đã đăng nhập
    @GET("api/recipes/search/auth")
    suspend fun searchAuthRecipes(
        @Query("search") search: String
    ): Response<RecipeResponse>

    @GET("api/search-history/{userId}")
    suspend fun getSearchHistory(@Path("userId") userId: Int): Response<SearchHistoryResponse>

    @DELETE("api/search-history/{userId}/{id}")
    suspend fun deleteSearchHistory(
        @Path("userId") userId: Int,
        @Path("id") id: Int
    ): Response<SearchHistoryResponse>

    // Xóa toàn bộ lịch sử tìm kiếm
    @DELETE("api/search-history")
    suspend fun deleteAllSearchHistory(): Response<Void>

    // Lấy thông tin người dùng
    @GET("api/users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): Response<UserProfileResponse>

    @Multipart
    @POST("api/user/update/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: Int,
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("id_cooklab") idCookpad: RequestBody,
        @Part("password") password: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): Response<UserProfileResponse>

    @JvmSuppressWildcards
    @Multipart
    @POST("api/recipes")
    suspend fun createRecipe(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("category_id") categoryId: RequestBody,
        @Part("cook_time") cookTime: RequestBody,
        @Part("servings") servings: RequestBody,
        @Part("ingredients[]") ingredients: List<RequestBody>, // Sử dụng List<RequestBody>
        @Part("steps[][description]") stepDescriptions: List<RequestBody>, // Sử dụng List<RequestBody>
        @Part stepImages: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part // Ảnh chính của công thức
    ): Response<CreateRecipeResponse>

    @JvmSuppressWildcards
    @Multipart
    @POST("api/recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") recipeId: Int,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("category_id") categoryId: RequestBody,
        @Part("cook_time") cookTime: RequestBody,
        @Part("servings") servings: RequestBody,
        @Part("ingredients[]") ingredients: List<RequestBody>,
        @Part("steps[][description]") stepDescriptions: List<RequestBody>,
        @Part stepImages: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part
    ): Response<CreateRecipeResponse>

    @GET("api/recipes/saved-recipes/{userId}")
    suspend fun getSavedRecipes(
        @Path("userId") userId: Int
    ): Response<RecipeResponse>

    // Định nghĩa API xóa công thức
    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(
        @Path("id") recipeId: Int
    ): Response<BasicSaveResponse>

    // Lấy thông tin người dùng
    @GET("api/customer/{userId}")
    suspend fun getCustomerProfile(@Path("userId") userId: Int): Response<CustomerProfileResponse>

    // Trending
    @GET("api/trending")
    suspend fun getTrendingRecipes(): Response<RecipeResponse>
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

data class SearchHistoryResponse(
    val success: Boolean,
    val data: List<SearchHistory>,
    val message: String?
)

data class SearchHistory(
    val id: Int,
    val user_id: Int,
    val keyword: String,
    val searched_at: String
)

data class UserProfileResponse(
    val success: Boolean,
    val message: String?,
    val user: User,
    val recipes: List<Recipe>
)
data class CustomerProfileResponse(
    val success: Boolean,
    val user: User,
    val recipes: List<Recipe>
)



