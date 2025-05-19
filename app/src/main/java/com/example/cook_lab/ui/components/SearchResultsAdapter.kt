package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.databinding.ItemSearchBinding
import com.example.cook_lab.databinding.ItemSearchEmptyBinding
import com.example.cook_lab.ui.components.SearchResultsAdapter.SearchResultsViewHolder.EmptyViewHolder

class SearchResultsAdapter(private val onRecipeClick: (Recipe) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private val recipes = mutableListOf<Recipe>()

    fun setData(newData: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (recipes.isEmpty()) 1 else recipes.size

    override fun getItemViewType(position: Int): Int =
        if (recipes.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val binding = ItemSearchEmptyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            EmptyViewHolder(binding)
        } else {
            val binding = ItemSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SearchResultsViewHolder(binding, onRecipeClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchResultsViewHolder) {
            holder.bind(recipes[position])
        }
        // EmptyViewHolder không cần bind gì
    }

    class SearchResultsViewHolder(
        private val binding: ItemSearchBinding,
        private val onRecipeClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.recipeTitle.text = recipe.title
            binding.recipeDescription.text = recipe.description ?: "No description available"
            binding.recipeUserTitle.text = recipe.user?.name ?: "Unknown Author"

//            val imagePath = recipe.image?.removePrefix("/") ?: ""
//            val fullImageUrl = "http://192.168.88.157:8000/$imagePath"
//            Glide.with(binding.root)
//                .load(fullImageUrl)
//                .placeholder(R.drawable.error_image)
//                .error(R.drawable.error_image)
//                .into(binding.recipeImage)
//
//            val avatarPath = recipe.user?.avatar?.removePrefix("/") ?: ""
//            val avatarUrl = "http://192.168.88.157:8000/$avatarPath"
//            Glide.with(binding.root)
//                .load(avatarUrl)
//                .placeholder(R.drawable.account)
//                .error(R.drawable.account)
//                .circleCrop()
//                .into(binding.userAvatar)

            recipe.image?.let {
                val url = ApiClient.BASE_URL + it.removePrefix("/")
                Glide.with(binding.root)
                    .load(url)
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(binding.recipeImage)
            } ?: binding.recipeImage.setImageResource(R.drawable.error_image)

            recipe.user?.avatar?.let {
                val url = ApiClient.BASE_URL + it.removePrefix("/")
                Glide.with(binding.root)
                    .load(url)
                    .placeholder(R.drawable.account)
                    .error(R.drawable.account)
                    .circleCrop()
                    .into(binding.userAvatar)

                binding.root.setOnClickListener { onRecipeClick(recipe) }
            }
        }

        class EmptyViewHolder(binding: ItemSearchEmptyBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}

