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
import com.example.cook_lab.data.model.Category

class CategoryAdapter(
    private val onItemClick: (category: Category) -> Unit  // callback bây giờ trả về luôn Category
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = mutableListOf<Category>()

    fun setData(newData: List<Category>) {
        categories.clear()
        categories.addAll(newData)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.txtCategoryName)
        private val image: ImageView = itemView.findViewById(R.id.imgCategory)

        fun bind(category: Category) {
            name.text = category.name

            // Xử lý URL ảnh
            val imagePath = category.image?.removePrefix("/") ?: ""
//            val fullImageUrl = "http://192.168.88.157:8000/$imagePath"
            val fullImageUrl = ApiClient.BASE_URL + imagePath

            Glide.with(itemView)
                .load(fullImageUrl)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(image)

            itemView.setOnClickListener {
                onItemClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size
}
