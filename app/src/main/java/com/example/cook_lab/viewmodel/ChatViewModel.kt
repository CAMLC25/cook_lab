package com.example.cook_lab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cook_lab.data.model.ChatResponse
import com.example.cook_lab.data.model.CatalogHit
import com.example.cook_lab.data.repository.ChatRepository
import com.example.cook_lab.ui.chat.ChatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/* ===== UI State ===== */
sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()
    data class Success(val data: ChatResponse) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

/* ===== ViewModel ===== */
class ChatViewModel(private val repo: ChatRepository) : ViewModel() {

    // State cho loading/error (nếu bạn muốn show spinner/toast)
    private val _state = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    // Danh sách item để RecyclerView render
    private val _items = MutableStateFlow<List<ChatItem>>(emptyList())
    val items: StateFlow<List<ChatItem>> = _items.asStateFlow()

    /** Thêm 1 hoặc nhiều ChatItem vào cuối list */
    private fun append(vararg newItems: ChatItem) {
        _items.value = _items.value + newItems
    }

    /** Thêm list ChatItem vào cuối list */
    private fun appendAll(list: List<ChatItem>) {
        if (list.isEmpty()) return
        _items.value = _items.value + list
    }

    /** Gửi tin nhắn + gọi API */
    fun send(text: String, preferHowto: Boolean = false) {
        val msg = text.trim()
        if (msg.isBlank()) return

        // 1) Show bubble user ngay lập tức
        append(ChatItem.User(msg))

        // 2) Gọi API
        _state.value = ChatUiState.Loading
        viewModelScope.launch {
            try {
                val res = repo.ask(text = msg, preferHowto = preferHowto)
                _state.value = ChatUiState.Success(res)

                // 3) Map ChatResponse -> ChatItem để hiển thị
                appendAll(mapResponseToItems(res))

            } catch (e: Exception) {
                _state.value = ChatUiState.Error(e.message ?: "Network error")
                append(ChatItem.Note("Lỗi: ${e.message ?: "timeout"}"))
            }
        }
    }

    /** Chuyển ChatResponse -> List<ChatItem> để Adapter hiển thị */
    private fun mapResponseToItems(res: ChatResponse): List<ChatItem> {
        val items = mutableListOf<ChatItem>()

        // A) Greeting nếu intent = chit_chat
        if (res.intent.equals("chit_chat", ignoreCase = true)) {
            val chips = res.aiSuggestions.mapNotNull { it.title }
                .ifEmpty { listOf("Chào bạn!", "Gợi ý theo nguyên liệu", "Đồ uống / nước giải khát", "Chè / tráng miệng", "Cách nấu món cụ thể") }

            items += ChatItem.Greeting(
                title = "Xin chào 👋\nMình là CookLab — bạn muốn tìm món ăn, đồ uống hay xem cách nấu?",
                subtitle = "Gợi ý nhanh:",
                chips = chips
            )
        }

        // B) Các món tìm thấy trong kho (hiển thị card: ảnh + tên + thời gian)
        val hits: List<CatalogHit> = res.catalogHits
        hits.forEach { hit -> items += ChatItem.Catalog(hit) }

        // C) HowTo nếu có
        res.aiRecipe?.let { recipe -> items += ChatItem.HowTo(recipe) }

        // D) Note nếu backend có trả
        res.note?.takeIf { it.isNotBlank() }?.let { items += ChatItem.Note(it) }

        // E) Nếu không phải chit_chat mà có gợi ý AI -> gom thành Note
        if (!res.intent.equals("chit_chat", ignoreCase = true)) {
            val sugTitles = res.aiSuggestions.mapNotNull { it.title }
            if (sugTitles.isNotEmpty()) {
                val body = "Gợi ý thêm:\n• " + sugTitles.joinToString("\n• ")
                items += ChatItem.Note(body)
            }
        }

        return items
    }
}

/* ===== Factory để dùng viewModels{} trong Activity ===== */
class ChatViewModelFactory(private val repo: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(cls: Class<T>): T {
        if (cls.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${cls.name}")
    }
}
