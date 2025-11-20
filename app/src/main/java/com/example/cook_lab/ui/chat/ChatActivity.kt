package com.example.cook_lab.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cook_lab.R
import com.example.cook_lab.data.api.ApiClient
import com.example.cook_lab.data.model.CatalogHit
import com.example.cook_lab.data.repository.ChatRepository
import com.example.cook_lab.ui.recipe.RecipeDetailActivity
import com.example.cook_lab.ui.viewmodel.ChatUiState
import com.example.cook_lab.ui.viewmodel.ChatViewModel
import com.example.cook_lab.ui.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatActivity : ComponentActivity() {

    private lateinit var root: View
    private lateinit var recycler: RecyclerView
    private lateinit var input: EditText
    private lateinit var btnSend: ImageButton

    private val adapter by lazy {
        ChatAdapter(
            onChipClicked = { text -> send(text) },
            onCatalogClicked = { hit: CatalogHit ->
                // TODO: mở màn chi tiết recipe từ hit.id nếu bạn có màn RecipeDetail
                val recipeId = hit.id.toIntOrNull()
                if (recipeId != null) {
                    val intent = Intent(this, RecipeDetailActivity::class.java)
                    intent.putExtra("RECIPE_ID", recipeId)
                    startActivity(intent)
                }
                // startActivity(RecipeDetailActivity.newIntent(this, hit.id))
            }
        )
    }

    // ViewModel lifecycle-aware + Factory chuẩn
    private val vm: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepository(ApiClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        root = findViewById<View?>(R.id.root) ?: window.decorView
        recycler = findViewById(R.id.recycler)

        val header: View = findViewById(R.id.header)
        val composer: View = findViewById(R.id.composer)
        val btnClose: ImageView = header.findViewById(R.id.btnClose)
        input  = composer.findViewById(R.id.input)
        btnSend = composer.findViewById(R.id.btnSend)

        btnClose.setOnClickListener { finish() }

        recycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recycler.adapter = adapter
        recycler.setHasFixedSize(false)
        recycler.setItemViewCacheSize(20)

        recycler.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) scrollToBottom()
        }

        composer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recycler.updatePadding(bottom = composer.height)
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val nav = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottom = if (ime.bottom > 0) ime.bottom else nav.bottom
            recycler.updatePadding(bottom = composer.height + extraBottom)
            insets
        }

        input.imeOptions = EditorInfo.IME_ACTION_SEND
        btnSend.setOnClickListener { send(input.text.toString()) }
        input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { send(v.text.toString()); true } else false
        }

        // Quan sát danh sách item để render
        vm.items.onEach { list ->
            adapter.submitList(list.toList()) { scrollToBottom() }
        }.launchIn(lifecycleScope)

        // Quan sát state để show lỗi/typing nếu muốn (VM đã tự append Note khi lỗi)
        vm.state.onEach { s ->
            when (s) {
                is ChatUiState.Loading -> { /* optional: show typing indicator */ }
                is ChatUiState.Error   -> { /* VM đã thêm bubble lỗi; có thể toast thêm nếu muốn */ }
                is ChatUiState.Success -> { /* Nội dung đã được append vào items */ }
                else -> Unit
            }
        }.launchIn(lifecycleScope)
    }

    private fun send(raw: String) {
        val msg = raw.trim()
        if (msg.isEmpty()) return
        hideKeyboard(input)
        input.setText("")
        vm.send(msg) // VM sẽ tự append ChatItem.User và gọi API
    }

    private fun hideKeyboard(view: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun scrollToBottom() {
        val last = adapter.itemCount - 1
        if (last >= 0) recycler.scrollToPosition(last)
    }
}
