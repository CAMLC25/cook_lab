package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Recipe

class CustomerRecipesAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Int) -> Unit // Callback chỉ cho việc xem chi tiết
) : RecyclerView.Adapter<CustomerRecipesAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)

        // Xử lý sự kiện nhấn vào item
        holder.itemView.setOnClickListener {
            onItemClick(recipe.id) // Chuyển đến chi tiết công thức
        }
    }

    override fun getItemCount(): Int = recipes.size

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitle)
        private val recipeDescription: TextView = itemView.findViewById(R.id.recipeDescription)
        private val tvCookTime: TextView = itemView.findViewById(R.id.tvCookTime)
        private val tvServings: TextView = itemView.findViewById(R.id.tvServings)
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)

        fun bind(recipe: Recipe) {
            recipeTitle.text = recipe.title
            recipeDescription.text = recipe.description
            tvCookTime.text = recipe.cook_time.toString()
            tvServings.text = recipe.servings.toString()
            Glide.with(itemView.context)
                .load(ApiClient.BASE_URL + recipe.image)
                .into(recipeImage)
        }
    }
}


