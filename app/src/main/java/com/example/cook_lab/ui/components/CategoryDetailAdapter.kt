package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.model.Recipe

class CategoryDetailAdapter(
    private val categoryTitle: String
) : RecyclerView.Adapter<CategoryDetailAdapter.CategoryDetailViewHolder>() {

    private val recipes = mutableListOf<Recipe>()

    fun setData(newData: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_detail, parent, false)
        return CategoryDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryDetailViewHolder, position: Int) {
        val recipe = recipes[position]

//        // Bind category title (for first item only)
//        if (position == 0) {
//            holder.categoryTitle.visibility = View.VISIBLE
//            holder.categoryTitle.text = categoryTitle
//        } else {
//            holder.categoryTitle.visibility = View.GONE
//        }

        // Bind recipe details
        holder.recipeTitle.text = recipe.title
        holder.recipeDescription.text = recipe.description ?: ""
        holder.recipeUserTitle.text = recipe.user.name

        // Load recipe image
        val imagePath = recipe.image?.removePrefix("/") ?: ""
        val fullImageUrl = "http://192.168.88.157:8000/$imagePath"
        Glide.with(holder.itemView)
            .load(fullImageUrl)
            .placeholder(R.drawable.error_image)
            .error(R.drawable.error_image)
            .into(holder.recipeImage)

        // Load user avatar
        val avatarPath = recipe.user.avatar?.removePrefix("/") ?: ""
        val avatarUrl = "http://192.168.88.157:8000/$avatarPath"
        Glide.with(holder.itemView)
            .load(avatarUrl)
            .placeholder(R.drawable.account)
            .error(R.drawable.account)
            .circleCrop()
            .into(holder.userAvatar)

        // Bookmark button state
//        holder.saveRecipe.setImageResource(
//            if (recipe.isFavorited) R.drawable.ic_bookmark_filled
//            else R.drawable.ic_bookmark_outline
//        )
//        holder.saveRecipe.setOnClickListener {
//            // Handle bookmark toggle
//            recipe.isFavorited = !recipe.isFavorited
//            notifyItemChanged(position)
//            // Optionally callback to listener
//        }
    }

    override fun getItemCount(): Int = recipes.size

    class CategoryDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val categoryTitle: TextView = itemView.findViewById(R.id.textViewCategoryOfRecipes)
        val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitle)
        val recipeDescription: TextView = itemView.findViewById(R.id.recipeDescription)
        val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        val recipeUserTitle: TextView = itemView.findViewById(R.id.recipeUserTitle)
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
//        val saveRecipe: ImageButton = itemView.findViewById(R.id.saveRecipe)
    }
}
