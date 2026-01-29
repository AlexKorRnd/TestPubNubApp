package com.example.testpubnubapp.models

data class ChatMessage(
    val text: String,
    val sender: String,
    val timestampEpochMillis: Long,
    val isHistory: Boolean = false
)
