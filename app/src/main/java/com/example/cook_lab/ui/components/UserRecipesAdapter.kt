package com.example.cook_lab.ui.components

import android.content.Intent
import android.util.Log
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
import com.example.cook_lab.ui.recipe.RecipeDetailActivity

class UserRecipeAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<UserRecipeAdapter.RecipeViewHolder>() {
    init {
        Log.e("UserRecipeAdapter", "Recipes: $recipes")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)
        Log.e("UserRecipeAdapter", "Binding recipe: $recipe")
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

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                itemView.context.startActivity(intent)
            }
        }
    }
}
