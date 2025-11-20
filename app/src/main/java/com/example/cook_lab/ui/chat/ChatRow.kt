// app/src/main/java/com/example/cook_lab/ui/chat/ChatRow.kt
package com.example.cook_lab.ui.chat

enum class Role { USER, BOT }

data class ChatRow(
    val id: Long,
    val role: Role,
    val text: String
)
