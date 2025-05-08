package com.example.cook_lab.ui.components

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Step

class StepAdapter : ListAdapter<Step, StepAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Step>() {
            override fun areItemsTheSame(old: Step, new: Step) = old.id == new.id
            override fun areContentsTheSame(old: Step, new: Step) = old == new
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumber = itemView.findViewById<TextView>(R.id.tvStepNumber)
        private val tvDesc   = itemView.findViewById<TextView>(R.id.tvStepDesc)
        private val img      = itemView.findViewById<ImageView>(R.id.imgStep)

        fun bind(step: Step) {
            // 1) Số bước
            tvNumber.text = "Bước ${step.step_number}"

            // 2) Mô tả, justify nếu API >= 26
            tvDesc.text = step.description
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tvDesc.justificationMode = android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
            }

            // 3) Ảnh bước
            val raw = step.image?.removePrefix("/")
            if (!raw.isNullOrBlank()) {
                img.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load("${ApiClient.BASE_URL}$raw")
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(img)

                // 4) Click để phóng to
                img.setOnClickListener {
                    showImageDialog("${ApiClient.BASE_URL}$raw")
                }
            } else {
                img.visibility = View.GONE
                img.setOnClickListener(null)
            }
        }

        private fun showImageDialog(url: String) {
            val dialogView = LayoutInflater.from(itemView.context)
                .inflate(R.layout.dialog_image_preview, null, false)
            val preview = dialogView.findViewById<ImageView>(R.id.previewImage)

            Glide.with(itemView.context)
                .load(url)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(preview)

            val dialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .create()

            // Cho nền dialog trong suốt
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_step, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
