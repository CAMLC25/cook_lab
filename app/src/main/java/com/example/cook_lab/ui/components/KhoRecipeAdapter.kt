package com.example.cook_lab.ui.components

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Recipe
import com.example.cook_lab.ui.recipe.RecipeDetailActivity

class KhoRecipeAdapter(private var recipes: List<Recipe>, private val onRemoveSavedRecipe: (Int) -> Unit) : RecyclerView.Adapter<KhoRecipeAdapter.RecipeViewHolder>() {

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

        // Xử lý sự kiện nhấn giữ vào item
        holder.itemView.setOnLongClickListener {
            showRemoveRecipeDialog(it.context, recipe.id) // Sửa chỗ này để sử dụng context đúng
            true // Trả về true để xử lý sự kiện nhấn giữ
        }
    }

    override fun getItemCount(): Int = recipes.size

    // Phương thức để hiển thị dialog xác nhận bỏ lưu công thức
    private fun showRemoveRecipeDialog(context: Context, recipeId: Int) {
        val dialog = AlertDialog.Builder(context) // Sử dụng context đúng
            .setTitle("Bỏ lưu công thức?")
            .setMessage("Bạn có muốn xóa công thức này khỏi danh sách đã lưu không?")
            .setPositiveButton("Có") { _, _ ->
                // Gọi ViewModel để xóa công thức khỏi danh sách đã lưu
                onRemoveSavedRecipe(recipeId)
            }
            .setNegativeButton("Không", null)
            .create()

        dialog.show()
    }

    // Cập nhật danh sách công thức trong Adapter
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

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
