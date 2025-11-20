package com.example.cook_lab.data.repository

import com.example.cook_lab.data.api.ApiService
import com.example.cook_lab.data.model.ChatResponse

class ChatRepository(private val api: ApiService) {
    suspend fun ask(text: String, preferHowto: Boolean = false): ChatResponse {
        val body = mapOf(
            "text" to text,
            "prefer_howto" to preferHowto
        )
        return api.ask(body)
    }
}
