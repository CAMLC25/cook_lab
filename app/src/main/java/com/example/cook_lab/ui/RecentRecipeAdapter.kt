package com.example.cook_lab.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cook_lab.R
import com.example.cook_lab.model.Recipe

class RecentRecipeAdapter(
    private val recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit // Callback khi nhấn vào item
) : RecyclerView.Adapter<RecentRecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recentImageView: ImageView = itemView.findViewById(R.id.recentImageView)
        val recentNameTextView: TextView = itemView.findViewById(R.id.recentNameTextView)
        val recentTimeTextView: TextView = itemView.findViewById(R.id.recentTimeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recentNameTextView.text = recipe.name
        holder.recentTimeTextView.text = recipe.searchTime
        // TODO: Tải hình ảnh từ API hoặc nguồn dữ liệu cho recentImageView tại đây

        // Thiết lập sự kiện nhấn cho item
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size
}
