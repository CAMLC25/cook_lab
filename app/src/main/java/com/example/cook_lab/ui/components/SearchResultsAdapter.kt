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
import com.example.cook_lab.databinding.ItemSearchBinding

class SearchResultsAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder>() {

    private val recipes = mutableListOf<Recipe>()

    fun setData(newData: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultsViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)  // Sử dụng ViewBinding
        return SearchResultsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultsViewHolder, position: Int) {
        val recipe = recipes[position]

        // Bind thông tin công thức
        holder.binding.recipeTitle.text = recipe.title
        holder.binding.recipeDescription.text = recipe.description ?: "No description available"
        holder.binding.recipeUserTitle.text = recipe.user?.name ?: "Unknown Author"

        // Load ảnh công thức
        val imagePath = recipe.image?.removePrefix("/") ?: ""
        val fullImageUrl = "http://192.168.88.157:8000/$imagePath"
        Glide.with(holder.itemView)
            .load(fullImageUrl)
            .placeholder(R.drawable.error_image)
            .error(R.drawable.error_image)
            .into(holder.binding.recipeImage)

        // Load avatar người dùng
        val avatarPath = recipe.user?.avatar?.removePrefix("/") ?: ""
        val avatarUrl = "http://192.168.88.157:8000/$avatarPath"
        Glide.with(holder.itemView)
            .load(avatarUrl)
            .placeholder(R.drawable.account)
            .error(R.drawable.account)
            .circleCrop()
            .into(holder.binding.userAvatar)

        // Xử lý sự kiện khi click vào công thức
        holder.itemView.setOnClickListener {
            onRecipeClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    class SearchResultsViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)
}

