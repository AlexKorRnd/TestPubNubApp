package com.example.testpubnubapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testpubnubapp.models.ChatMessage
import com.example.testpubnubapp.models.UserPresence
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var pubNubManager: PubNubManager? = null

    fun connect(username: String) {
        if (pubNubManager != null) return

        _uiState.update { it.copy(currentUserId = username) }

        pubNubManager = PubNubManager(
            username = username,
            onMessageReceived = { payload, isHistory ->
                addMessageFromPayload(payload, isHistory)
            },
            onPresenceChange = { userId, isOnline ->
                updatePresence(userId, isOnline)
            },
            onStatus = { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            },
            onError = { error ->
                _uiState.update { it.copy(errorMessage = error) }
            }
        ).also { manager ->
            manager.subscribe(PubNubManager.CHANNEL_NAME)
            manager.hereNow(PubNubManager.CHANNEL_NAME)
            manager.fetchHistory(PubNubManager.CHANNEL_NAME)
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        val mentions = extractMentions(text)
        pubNubManager?.publish(PubNubManager.CHANNEL_NAME, text, chatId, mentions, null)
    }

    fun sendImage(chatId: String, imageBase64: String) {
        val trimmed = imageBase64.trim()
        if (trimmed.isBlank()) return
        val manager = pubNubManager
        if (manager == null) {
            Log.e("MessageViewModel", "sendImage failed: PubNubManager not initialized")
            return
        }
        manager.publish(PubNubManager.CHANNEL_NAME, "", chatId, emptyList(), trimmed)
    }

    fun refreshHistory() {
        pubNubManager?.fetchHistory(PubNubManager.CHANNEL_NAME)
    }

    private fun addMessageFromPayload(payload: JsonObject, isHistory: Boolean) {
        val mentionsFromPayload = payload.getAsJsonArray("mentions")
            ?.mapNotNull { element -> element.takeIf { it.isJsonPrimitive }?.asString }
            .orEmpty()
        val messageText = payload.get("text")?.asString.orEmpty()
        val imageBase64 = payload.get("imageBase64")?.asString
        val mentions = if (mentionsFromPayload.isNotEmpty()) {
            mentionsFromPayload
        } else {
            extractMentions(messageText)
        }
        val message = ChatMessage(
            text = messageText,
            sender = payload.get("sender")?.asString.orEmpty(),
            chatId = payload.get("chatId")?.asString ?: PubNubManager.CHANNEL_NAME,
            timestampEpochMillis = payload.get("timestampEpochMillis")?.asLong
                ?: System.currentTimeMillis(),
            imageBase64 = imageBase64,
            mentions = mentions,
            isHistory = isHistory
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun updatePresence(userId: String, isOnline: Boolean) {
        _uiState.update { state ->
            val updated = state.onlineUsers.toMutableSet()
            if (isOnline) {
                updated.add(UserPresence(userId))
            } else {
                updated.remove(UserPresence(userId))
            }
            state.copy(onlineUsers = updated.toList())
        }
    }

    fun markChatRead(chatId: String) {
        _uiState.update { state ->
            val latestTimestamp = state.messages
                .filter { it.chatId == chatId }
                .maxOfOrNull { it.timestampEpochMillis }
                ?: System.currentTimeMillis()
            val updatedMap = state.lastReadAt.toMutableMap()
            updatedMap[chatId] = latestTimestamp
            state.copy(lastReadAt = updatedMap)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pubNubManager?.disconnect()
    }

    private fun extractMentions(text: String): List<String> {
        val regex = Regex("@([A-Za-z0-9_.-]+)")
        return regex.findAll(text)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val onlineUsers: List<UserPresence> = emptyList(),
    val currentUserId: String? = null,
    val connectionStatus: String = "Connecting",
    val errorMessage: String? = null,
    val lastReadAt: Map<String, Long> = emptyMap()
)
