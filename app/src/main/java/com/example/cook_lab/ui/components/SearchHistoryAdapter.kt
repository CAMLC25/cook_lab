package com.example.cook_lab.ui.components

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cook_lab.R
import com.example.cook_lab.data.api.SearchHistory
import com.example.cook_lab.databinding.ItemSearchHistoryBinding
import com.example.cook_lab.databinding.ItemSearchHistoryEmptyBinding
import java.text.SimpleDateFormat
import java.util.*

class SearchHistoryAdapter(
    private val onDeleteClick: (SearchHistory) -> Unit,
    private val onItemClick: (SearchHistory) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private val items = mutableListOf<SearchHistory>()

    fun setData(newData: List<SearchHistory>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    fun removeItem(history: SearchHistory) {
        val index = items.indexOf(history)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
            // Nếu sau khi xóa không còn mục nào, thêm view empty vào vị trí 0
            if (items.isEmpty()) notifyItemInserted(0)
        }
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 1 else items.size

    override fun getItemViewType(position: Int): Int =
        if (items.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_EMPTY) {
            val binding = ItemSearchHistoryEmptyBinding.inflate(inflater, parent, false)
            EmptyViewHolder(binding)
        } else {
            val binding = ItemSearchHistoryBinding.inflate(inflater, parent, false)
            HistoryViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryViewHolder) {
            holder.bind(items[position])
        }
        // EmptyViewHolder needs no binding
    }

    inner class HistoryViewHolder(
        private val binding: ItemSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: SearchHistory) {
            binding.searchTermText.text = history.keyword
            val parsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(history.searched_at) ?: Date()

            val timeStr = when {
                DateUtils.isToday(parsed.time) -> "Hôm nay"
                isYesterday(parsed) -> "Hôm qua"
                else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed)
            }
            binding.searchTimeText.text = timeStr

            binding.root.setOnClickListener { onItemClick(history) }
            binding.clearSearchHistory.setOnClickListener { onDeleteClick(history) }
        }

        private fun isYesterday(date: Date): Boolean {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            val yesterday = cal.time
            return date.after(yesterday) && date.before(Calendar.getInstance().time)
        }
    }

    class EmptyViewHolder(binding: ItemSearchHistoryEmptyBinding) : RecyclerView.ViewHolder(binding.root)
}

