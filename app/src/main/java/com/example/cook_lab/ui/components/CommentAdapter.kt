package com.example.cook_lab.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.Comment
import com.example.cook_lab.ui.components.NewRecipesAdapter.Companion.computeRelativeTime
import com.google.android.material.imageview.ShapeableImageView
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CommentAdapter(private val onItemLongClick: (Comment) -> Unit) : ListAdapter<Comment, CommentAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(a: Comment, b: Comment) = a.id == b.id
            override fun areContentsTheSame(a: Comment, b: Comment) = a == b

            // Hàm tính thời gian tương đối
            fun computeRelativeTime(iso: String): String {
                return try {
                    val then = OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    val now = OffsetDateTime.now()
                    val minutes = ChronoUnit.MINUTES.between(then, now)
                    when {
                        minutes < 1    -> "vừa xong"
                        minutes < 60   -> "cách đây $minutes phút"
                        minutes < 1440 -> "cách đây ${minutes / 60} giờ"
                        else           -> "cách đây ${minutes / 1440} ngày"
                    }
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val avatar  = v.findViewById<ShapeableImageView>(R.id.imgCommentAvatar)
        private val user    = v.findViewById<TextView>(R.id.tvCommentUser)
        private val content = v.findViewById<TextView>(R.id.tvCommentContent)
        private val time    = v.findViewById<TextView>(R.id.tvCommentTime)

        fun bind(c: Comment) {
            // Tên và nội dung
            user.text    = c.user.name
            content.text = c.content
            // Thời gian
            time.text = computeRelativeTime(c.created_at)

            // Load avatar
            c.user.avatar?.let { path ->
                val url = ApiClient.BASE_URL.removeSuffix("/") + "/" + path.removePrefix("/")
                Glide.with(itemView)
                    .load(url)
                    .placeholder(R.drawable.account)
                    .circleCrop()
                    .into(avatar)
            }

            // Thiết lập sự kiện nhấn dài (long click)
            itemView.setOnLongClickListener {
                onItemLongClick(c)  // Gọi hàm xóa
                true // Trả về true để không tiếp tục sự kiện nhấn
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_comment, p, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))
}

