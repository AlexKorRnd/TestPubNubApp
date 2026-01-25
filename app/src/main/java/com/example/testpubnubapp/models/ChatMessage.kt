package com.example.testpubnubapp.models

data class ChatMessage(
    val text: String,
    val sender: String,
    val timestamp: String,
    val isHistory: Boolean = false
)
