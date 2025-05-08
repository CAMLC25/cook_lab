package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cook_lab.R
import com.example.cook_lab.data.model.Ingredient

class IngredientAdapter : ListAdapter<Ingredient, IngredientAdapter.VH>(DIFF) {
    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<Ingredient>() {
            override fun areItemsTheSame(a: Ingredient, b: Ingredient) = a.id == b.id
            override fun areContentsTheSame(a: Ingredient, b: Ingredient) = a == b
        }
    }
    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        private val name: TextView = v.findViewById(R.id.tvIngredientName)
        fun bind(i: Ingredient) { name.text = i.name }
    }
    override fun onCreateViewHolder(p: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_ingredient, p, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
}

