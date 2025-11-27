package com.example.cook_lab.ui.chat

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.AiRecipe
import com.example.cook_lab.data.model.CatalogHit
import com.google.android.flexbox.FlexboxLayout

/* ================= Chat items ================= */

sealed class ChatItem {
    data class User(val text: String) : ChatItem()
    data class Greeting(
        val title: String,
        val subtitle: String?,
        val chips: List<String>
    ) : ChatItem()
    data class Catalog(val hit: CatalogHit) : ChatItem()
    data class HowTo(val recipe: AiRecipe) : ChatItem()
    data class Note(val text: String) : ChatItem()
}

/* ================= Adapter ================= */

class ChatAdapter(
    private val onChipClicked: (String) -> Unit,
    private val onCatalogClicked: (CatalogHit) -> Unit
) : ListAdapter<ChatItem, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val VT_USER = 1
        private const val VT_GREETING = 2
        private const val VT_CATALOG = 3
        private const val VT_HOWTO = 4
        private const val VT_NOTE = 5

        private val DIFF = object : DiffUtil.ItemCallback<ChatItem>() {
            override fun areItemsTheSame(old: ChatItem, new: ChatItem): Boolean {
                if (old::class != new::class) return false
                return when (old) {
                    is ChatItem.User     -> old.text == (new as ChatItem.User).text
                    is ChatItem.Greeting -> old.title == (new as ChatItem.Greeting).title
                    is ChatItem.Catalog  -> old.hit.id == (new as ChatItem.Catalog).hit.id
                    is ChatItem.HowTo    -> old.recipe.title == (new as ChatItem.HowTo).recipe.title
                    is ChatItem.Note     -> old.text == (new as ChatItem.Note).text
                }
            }
            override fun areContentsTheSame(old: ChatItem, new: ChatItem) = old == new
        }

        /** Ảnh: ưu tiên thumb, fallback image; hỗ trợ path tương đối ghép BASE_URL */
        private fun extractThumbUrl(hit: CatalogHit): String? {
            val raw = hit.thumb?.takeIf { it.isNotBlank() } ?: hit.image
            if (raw.isNullOrBlank()) return null
            val trimmed = raw.trim()
            if (trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                trimmed.startsWith("data:")
            ) return trimmed
            val path = trimmed.removePrefix("/")
            return if (path.isBlank()) null else ApiClient.BASE_URL + path
        }

        private fun extractCookTime(hit: CatalogHit): String? =
            hit.cookTime?.trim().takeUnless { it.isNullOrBlank() }

        private fun extractTimeMin(hit: CatalogHit): Int? = hit.timeMin

        private fun View.dp(v: Int): Int =
            (v * resources.displayMetrics.density).toInt()
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ChatItem.User     -> VT_USER
        is ChatItem.Greeting -> VT_GREETING
        is ChatItem.Catalog  -> VT_CATALOG
        is ChatItem.HowTo    -> VT_HOWTO
        is ChatItem.Note     -> VT_NOTE
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (vt) {
            VT_USER     -> UserVH(inf.inflate(R.layout.item_chat_user, parent, false))
            VT_GREETING -> GreetingVH(inf.inflate(R.layout.item_chat_greeting, parent, false), onChipClicked)
            VT_CATALOG  -> CatalogVH(inf.inflate(R.layout.item_chat_catalog, parent, false), onCatalogClicked)
            VT_HOWTO    -> HowToVH(inf.inflate(R.layout.item_chat_howto, parent, false))
            else        -> NoteVH(inf.inflate(R.layout.item_chat_note, parent, false))
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
        when (val it = getItem(pos)) {
            is ChatItem.User     -> (h as UserVH).bind(it)
            is ChatItem.Greeting -> (h as GreetingVH).bind(it)
            is ChatItem.Catalog  -> (h as CatalogVH).bind(it.hit)
            is ChatItem.HowTo    -> (h as HowToVH).bind(it.recipe)
            is ChatItem.Note     -> (h as NoteVH).bind(it)
        }
    }

    /* =============== ViewHolders =============== */

    class UserVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.tvText)
        fun bind(item: ChatItem.User) { tv.text = item.text }
    }

    class NoteVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.tvText)
        fun bind(item: ChatItem.Note) { tv.text = item.text }
    }

    class GreetingVH(
        private val v: View,
        private val onChipClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = v.findViewById(R.id.tvSubtitle)
        private val flow: FlexboxLayout = v.findViewById(R.id.flowChips)

        fun bind(item: ChatItem.Greeting) {
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle ?: ""
            tvSubtitle.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE

            flow.removeAllViews()
            item.chips.forEach { text ->
                flow.addView(makeChip(text) { onChipClicked(text) })
            }
        }

        private fun makeChip(text: String, onClick: () -> Unit): TextView {
            return TextView(v.context).apply {
                setText(text)
                maxLines = 3
                isSingleLine = false
                setPadding(v.dp(12), v.dp(8), v.dp(12), v.dp(8))
                setTextColor(0xFF111111.toInt())
                textSize = 14f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = v.dp(18).toFloat()
                    setColor(0xFFF3F4F6.toInt())
                    setStroke(v.dp(1), 0xFFE5E7EB.toInt())
                }
                val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, v.dp(8), v.dp(8))
                lp.flexGrow = 1f
                lp.flexShrink = 0f
                layoutParams = lp
                setOnClickListener { onClick() }
            }
        }
    }

    class CatalogVH(
        private val v: View,
        private val onCatalogClicked: (CatalogHit) -> Unit
    ) : RecyclerView.ViewHolder(v) {
        private val iv: ImageView = v.findViewById(R.id.ivThumb)
        private val tv: TextView = v.findViewById(R.id.tvTitle)
        private val tvTime: TextView? = v.findViewById(R.id.tvTime)

        fun bind(hit: CatalogHit) {
            tv.text = hit.title
            val url = extractThumbUrl(hit)
            if (url.isNullOrBlank()) {
                iv.setImageResource(R.drawable.error_image)
            } else {
                Glide.with(iv)
                    .load(url)
                    .apply(RequestOptions().centerCrop().transform(RoundedCorners(v.dp(10))))
                    .placeholder(R.drawable.error_image)
                    .error(R.drawable.error_image)
                    .into(iv)
            }

            val cook = extractCookTime(hit)
            val min  = extractTimeMin(hit)
            when {
                !cook.isNullOrBlank() -> { tvTime?.visibility = View.VISIBLE; tvTime?.text = "⏱ $cook" }
                min != null && min > 0 -> { tvTime?.visibility = View.VISIBLE; tvTime?.text = "⏱ ${min} phút" }
                else -> tvTime?.visibility = View.GONE
            }

            v.setOnClickListener { onCatalogClicked(hit) }
        }
    }

    class HowToVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        private val tvMeta:  TextView = v.findViewById(R.id.tvMeta)
        private val tvIngs:  TextView = v.findViewById(R.id.tvIngs)
        private val tvSteps: TextView = v.findViewById(R.id.tvSteps)
        private val tvTips:  TextView = v.findViewById(R.id.tvTips)

        fun bind(recipe: AiRecipe) {
            tvTitle.text = recipe.title.ifBlank { "Cách nấu" }

            // Meta
            val parts = mutableListOf<String>()
            recipe.servings?.let { if (it > 0) parts.add("👨‍🍳 Khẩu phần: $it") }
            recipe.timeMin?.let { if (it > 0) parts.add("⏱ Thời gian: ${it} phút") }
            tvMeta.text = parts.joinToString(" · ")
            tvMeta.visibility = if (parts.isEmpty()) View.GONE else View.VISIBLE

            // Ingredients (bullet)
            val ings = recipe.ingredients
            if (ings.isNullOrEmpty()) {
                tvIngs.visibility = View.GONE
            } else {
                tvIngs.visibility = View.VISIBLE
                tvIngs.text = spanSection("Nguyên liệu", bullets(ings))
            }

            // Steps (numbered)
            val steps = recipe.steps
            if (steps.isNullOrEmpty()) {
                tvSteps.text = "• Chưa có hướng dẫn chi tiết."
            } else {
                tvSteps.text = spanSection("Cách làm", numbered(steps))
            }

            // Tips
            val tips = recipe.tips
            if (tips.isNullOrEmpty()) {
                tvTips.visibility = View.GONE
            } else {
                tvTips.visibility = View.VISIBLE
                tvTips.text = spanSection("Mẹo", bullets(tips))
            }
        }

        /* ---------- formatting helpers ---------- */

        private fun spanSection(title: String, body: CharSequence): SpannableStringBuilder {
            val sb = SpannableStringBuilder()
            val start = sb.length
            sb.append(title).append("\n")
            sb.setSpan(
                StyleSpan(Typeface.BOLD),
                start, start + title.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            sb.append(body)
            return sb
        }

        private fun bullets(items: List<String>): CharSequence {
            val sb = SpannableStringBuilder()
            items.forEach { s ->
                sb.append("• ").append(s.trim()).append("\n")
            }
            return sb.trim()
        }

        private fun numbered(items: List<String>): CharSequence {
            val sb = SpannableStringBuilder()
            items.forEachIndexed { i, s ->
                val prefix = "${i + 1}. "
                val start = sb.length
                sb.append(prefix)
                sb.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start, start + prefix.length - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sb.append(s.trim()).append("\n")
            }
            return sb.trim()
        }
    }
}
