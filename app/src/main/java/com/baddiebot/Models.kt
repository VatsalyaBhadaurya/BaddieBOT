package com.baddiebot

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: BotMood = BotMood.SASSY
)

enum class BotMood {
    SASSY,
    DARK,
    PLAYFUL
} 