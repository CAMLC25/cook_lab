package com.example.cook_lab.ui.recipe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.api.Prefs
import com.example.cook_lab.data.model.Comment
import com.example.cook_lab.data.model.Reaction
import com.example.cook_lab.data.model.User
import com.example.cook_lab.databinding.ActivityRecipeDetailBinding
import com.example.cook_lab.ui.components.CommentAdapter
import com.example.cook_lab.ui.components.IngredientAdapter
import com.example.cook_lab.ui.components.StepAdapter
import com.example.cook_lab.viewmodel.FollowViewModel
import com.example.cook_lab.viewmodel.RecipeDetailViewModel
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipeDetailBinding
    private val vm by lazy {
        ViewModelProvider(this)[RecipeDetailViewModel::class.java]
    }
    private val followViewModel by lazy {
        ViewModelProvider(this)[FollowViewModel::class.java]
    }
    private val gson = Gson()
    private var recipeId = -1
    private var isSaved = false
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar & Collapsing
        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarDetail.setNavigationOnClickListener { finish() }
        binding.collapsingToolbar.title = ""

        // RecyclerViews
        binding.rvIngredients.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
            adapter = IngredientAdapter()
        }
        binding.rvSteps.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
            adapter = StepAdapter()
        }

        // Create CommentAdapter with long click listener
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
            adapter = CommentAdapter { comment ->
                showDeleteCommentDialog(comment)
            }
            visibility = View.GONE
        }
        binding.tvViewAllComments.visibility = View.GONE

        // Observe follow status
        followViewModel.isFollowing.observe(this) { isFollowed ->
            isFollowing = isFollowed
            updateFollowButton()
        }

        // Lấy ID & load
        recipeId = intent.getIntExtra("RECIPE_ID", -1)
        if (recipeId < 0) {
            Toast.makeText(this, "ID không hợp lệ", Toast.LENGTH_SHORT).show()
            finish(); return
        }
        vm.loadRecipe(recipeId)

        // Observe recipe
        vm.recipe.observe(this) { r ->
            Glide.with(this).load(r.image).into(binding.headerImage)
            binding.collapsingToolbar.title = r.title

            binding.tvTitle.text           = r.title
            binding.tvCookTime.text        = r.cook_time
            binding.tvServings.text        = r.servings
            binding.recipeDescription.text = "#${r.category?.name ?: "Chưa có danh mục"}\n${r.description}"
            binding.tvRecipeId.text = "ID Công thức: ${r.id}"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            try {
                val date = inputFormat.parse(r.created_at)
                val formattedDate = outputFormat.format(date)
                binding.tvRecipeDayCreate.text = "Lên sóng vào ngày $formattedDate"
                val date1 = inputFormat.parse(r.updated_at)
                val formattedDate1 = outputFormat.format(date1)
                if (formattedDate != formattedDate1)
                binding.tvRecipeDayUpdate.text = "Cập nhật vào ngày $formattedDate1"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Handle the bookmark click
            binding.btnBookmark.setOnClickListener {
                if (isSaved) {
                    showUnsaveDialog()  // Remove from saved list with confirmation
                } else {
                    saveRecipe()  // Add to saved list
                }
            }

            // Tác giả
            r.user.avatar?.removePrefix("/")?.let { path ->
                Glide.with(this)
                    .load(ApiClient.BASE_URL + path)
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(binding.imgAuthor)
            } ?: binding.imgAuthor.setImageResource(R.drawable.account)
            binding.recipeUserTitle.text = r.user.name
            binding.userIdCooklab.text   = "ID: @${r.user.id_cooklab}"

            r.user.avatar?.removePrefix("/")?.let { path ->
                Glide.with(this)
                    .load(ApiClient.BASE_URL + path)
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(binding.imgAuthor2)
            } ?: binding.imgAuthor.setImageResource(R.drawable.account)
            binding.tvAuthorName.text = r.user.name

            // Avatar current user
            Prefs.userJson?.let {
                val me = gson.fromJson(it, User::class.java)
                me.avatar?.removePrefix("/")?.let { path ->
                    Glide.with(this)
                        .load(ApiClient.BASE_URL + path)
                        .placeholder(R.drawable.account)
                        .circleCrop()
                        .into(binding.imgCurrentUser)
                }
            }

            // Lists
            (binding.rvIngredients.adapter as IngredientAdapter).submitList(r.ingredients)
            (binding.rvSteps.adapter    as StepAdapter)     .submitList(r.steps)
            (binding.rvComments.adapter as CommentAdapter)   .submitList(r.comments)

            // Reaction counts
            val counts = r.reactions.groupingBy { it.type }.eachCount()
            binding.likeCount.text = (counts["heart"] ?: 0).toString()
            binding.mlemCount.text = (counts["mlem"]  ?: 0).toString()
            binding.clapCount.text = (counts["clap"]  ?: 0).toString()

            // Comments header / toggle
            val c = r.comments.size
            binding.tvCommentHeader.text = "Bình luận $c"
            if (c > 0) {
                binding.tvViewAllComments.visibility = View.VISIBLE
                binding.tvViewAllComments.setOnClickListener {
                    binding.rvComments.visibility = View.VISIBLE
                    it.visibility = View.GONE
                }
            }

            // Kiểm tra trạng thái lưu công thức
            checkIfRecipeIsSaved()

            followViewModel.error.observe(this) { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }


            // Lấy ID của người đăng công thức
            val authorId = r.user.id

            // Kiểm tra trạng thái follow
            followViewModel.checkIfUserFollows(authorId)
            Log.e("FollowViewModel", "Check follow status success")

            // Handle Follow/Unfollow button click
            binding.btnFollow.setOnClickListener {
                if (isFollowing) {
                    followViewModel.unfollowUser(authorId)
                    Log.e("FollowViewModel", "Unfollow user success")
                } else {
                    followViewModel.followUser(authorId)
                }
            }
        }

        // Click reactions
        binding.ivHeart.setOnClickListener { postOrRemoveReaction("heart") }
        binding.ivMlem.setOnClickListener { postOrRemoveReaction("mlem") }
        binding.ivClap.setOnClickListener { postOrRemoveReaction("clap") }

        // Post comment
        binding.btnPostComment.setOnClickListener {
            val txt = binding.etComment.text.toString().trim()
            if (txt.isNotEmpty()) vm.postComment(recipeId, txt)
            else binding.etComment.error = "Bạn chưa nhập bình luận"
        }

        // Observe results
        vm.reactionPosted.observe(this) { type ->
            type?.let {
                when (it) {
                    "heart" -> binding.likeCount.text = "${binding.likeCount.text.toString().toInt() + 1}"
                    "mlem"  -> binding.mlemCount.text = "${binding.mlemCount.text.toString().toInt() + 1}"
                    "clap"  -> binding.clapCount.text = "${binding.clapCount.text.toString().toInt() + 1}"
                }
                Toast.makeText(this, "Bạn đã thả $it", Toast.LENGTH_SHORT).show()
            }
        }

        vm.commentPosted.observe(this) { cm ->
            cm?.let {
                val a = binding.rvComments.adapter as CommentAdapter
                val list = a.currentList.toMutableList().apply { add(it) }
                a.submitList(list)
                binding.rvComments.visibility = View.VISIBLE
                binding.tvViewAllComments.visibility = View.GONE
                binding.rvComments.scrollToPosition(list.size - 1)
                binding.etComment.text?.clear()
                Toast.makeText(this, "Đã gửi bình luận!", Toast.LENGTH_SHORT).show()
            }
        }

        vm.error.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }

    // Phương thức kiểm tra và gọi API để thả hoặc hủy phản ứng
    private fun postOrRemoveReaction(type: String) {
        // Kiểm tra xem người dùng đã thả biểu tượng cảm xúc này chưa
        val existingReaction = findExistingReaction(type)

        if (existingReaction != null) {
            // Nếu đã thả, gọi API để hủy thả phản ứng
            vm.removeReaction(recipeId, type)
        } else {
            // Nếu chưa thả, gọi API để thả phản ứng
            vm.postReaction(recipeId, type)
        }
    }

    // Tìm phản ứng của người dùng (tạm thời sử dụng trạng thái từ ViewModel)
    private fun findExistingReaction(type: String): Reaction? {
        return vm.recipe.value?.reactions?.find { it.type == type && it.user_id == Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id } }
    }


    // Kiểm tra xem người dùng hiện tại có lưu công thức hay không
    private fun checkIfRecipeIsSaved() {
        val currentUserId = Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id }
        isSaved = vm.recipe.value?.saved_by_users?.any { it.id == currentUserId } ?: false
        updateBookmarkIcon()
    }

    // Cập nhật icon bookmark dựa trên trạng thái isSaved
    private fun updateBookmarkIcon() {
        if (isSaved) {
            // Sử dụng setImageDrawable() để cập nhật icon khi đã lưu
            val drawable = ContextCompat.getDrawable(this, R.drawable.favorite_red)
            binding.btnBookmark.setImageDrawable(drawable)  // Icon khi đã lưu
        } else {
            // Cập nhật icon khi chưa lưu
            val drawable = ContextCompat.getDrawable(this, R.drawable.favorite)
            binding.btnBookmark.setImageDrawable(drawable)
        }
    }

    // Phương thức lưu hoặc hủy lưu công thức
    private fun saveOrRemoveRecipe() {
        val currentUserId_1 = Prefs.userJson?.let { gson.fromJson(it, User::class.java)?.id }
        isSaved = vm.recipe.value?.saved_by_users?.any { it.id == currentUserId_1 } ?: false

        if (isSaved) {
            vm.removeSavedRecipe(recipeId,)
        } else {
            vm.saveRecipe(recipeId)
        }
    }

    // Hàm hiển thị hộp thoại xác nhận xóa bình luận
    private fun showDeleteCommentDialog(comment: Comment) {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Bạn có chắc chắn muốn xóa bình luận này không?")
            .setPositiveButton("Có") { _, _ ->
                vm.removeComment(recipeId, comment.id)  // Gọi API xóa bình luận
            }
            .setNegativeButton("Không", null)
            .create()

        dialog.show()
    }

    // Lưu công thức
    private fun saveRecipe() {
        saveOrRemoveRecipe()
        isSaved = true
        checkIfRecipeIsSaved()
    }

    // Hủy lưu công thức
    private fun unsaveRecipe() {
        saveOrRemoveRecipe()
        isSaved = false
        checkIfRecipeIsSaved()
    }
    // Hiển thị hộp thoại xác nhận bỏ lưu
    private fun showUnsaveDialog() {
        val dialog = AlertDialog.Builder(this)
            .setMessage("Bạn có muốn bỏ lưu công thức này?")
            .setPositiveButton("Có") { _, _ -> unsaveRecipe() }
            .setNegativeButton("Không", null)
            .create()
        dialog.show()
    }

    private fun updateFollowButton() {
        // Cập nhật trạng thái của nút "Theo dõi" hoặc "Hủy theo dõi"
        val followText = if (isFollowing) "Đã theo dõi" else "Theo dõi"
        binding.btnFollow.text = followText
    }
}
