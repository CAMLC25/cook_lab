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

class RecipeAdapter : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    private val recipes = mutableListOf<Recipe>()

    fun setData(newData: List<Recipe>) {
        recipes.clear()
        recipes.addAll(newData)
        notifyDataSetChanged()
    }

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recipeTitle)
        val user_name: TextView = itemView.findViewById(R.id.recipeUserTitle)
        val image: ImageView = itemView.findViewById(R.id.recipeImage)
        val imgAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        holder.user_name.text = recipe.user.name
        // Xử lý ảnh - đảm bảo không có 2 dấu // trong URL
        val imagePath = recipe.image?.removePrefix("/") ?: ""
        val fullImageUrl = "http://192.168.88.157:8000/$imagePath"

        Glide.with(holder.itemView)
            .load(fullImageUrl)
            .placeholder(R.drawable.error_image) // ảnh tạm khi loading
            .error(R.drawable.error_image)       // ảnh hiển thị nếu lỗi
            .into(holder.image)

        val imagePathAvatar = recipe.user.avatar?.removePrefix("/") ?: ""
        val fullImageUrlAvatar = "http://192.168.88.157:8000/$imagePathAvatar"

        Glide.with(holder.itemView)
            .load(fullImageUrlAvatar)
            .placeholder(R.drawable.account) // ảnh tạm khi loading
            .error(R.drawable.account)       // ảnh hiển thị nếu lỗi
            .into(holder.imgAvatar)
    }

    override fun getItemCount(): Int = recipes.size
}