package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Recipe
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NewRecipesAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onBookmarkClick: (Recipe) -> Unit
) : ListAdapter<Recipe, NewRecipesAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView      = itemView.findViewById(R.id.recipeImage)
//        val btnBookmark: ImageButton = itemView.findViewById(R.id.saveRecipe)
        val txtTitle: TextView       = itemView.findViewById(R.id.recipeTitle)
        val txtDesc: TextView        = itemView.findViewById(R.id.recipeDescription)
        val imgAvatar: ImageView     = itemView.findViewById(R.id.userAvatar)
        val txtUser: TextView        = itemView.findViewById(R.id.recipeUserTitle)
        val txtTime: TextView        = itemView.findViewById(R.id.recipeTime)
        val txtLikes: TextView       = itemView.findViewById(R.id.likeCount)
        val txtMlems: TextView       = itemView.findViewById(R.id.mlemCount)
        val txtClaps: TextView       = itemView.findViewById(R.id.clapCount)

        fun bind(recipe: Recipe) {
            val ctx = itemView.context

            // 1. Ảnh chính
            recipe.image?.let {
                val url = ApiClient.BASE_URL + it.removePrefix("/")
                Glide.with(ctx)
                    .load(url)
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(imgPhoto)
            }

            // 2. Tiêu đề + mô tả
            txtTitle.text = recipe.title
            txtDesc.text  = recipe.description ?: ""

            // 3. Tác giả
            txtUser.text = recipe.user.name
            recipe.user.avatar?.let {
                val url = ApiClient.BASE_URL + it.removePrefix("/")
                Glide.with(ctx)
                    .load(url)
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(imgAvatar)
            }

            // 4. Thời gian
            txtTime.text = computeRelativeTime(recipe.updated_at)

            // 5. Phản ứng
            val counts = recipe.reactions.groupingBy { it.type }.eachCount()
            txtLikes.text = counts["heart"]?.toString() ?: "0"
            txtMlems.text = counts["mlem"]?.toString() ?: "0"
            txtClaps.text = counts["clap"]?.toString() ?: "0"

            // 6. Click listeners
            itemView.setOnClickListener { onItemClick(recipe) }
//            btnBookmark.setOnClickListener { onBookmarkClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_new_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(old: Recipe, new: Recipe) = old.id == new.id
            override fun areContentsTheSame(old: Recipe, new: Recipe) = old == new
        }

        // Hàm tính thời gian tương đối
        fun computeRelativeTime(iso: String): String {
            return try {
                val then    = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val now     = OffsetDateTime.now()
                val minutes = ChronoUnit.MINUTES.between(then, now)
                when {
                    minutes < 1    -> "vừa xong"
                    minutes < 60   -> "cách đây $minutes phút"
                    minutes < 1440 -> "cách đây ${minutes/60} giờ"
                    else           -> "cách đây ${minutes/1440} ngày"
                }
            } catch (e: Exception) {
                ""
            }
        }
    }
}
