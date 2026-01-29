package com.example.testpubnubapp

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

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        pubNubManager?.publish(PubNubManager.CHANNEL_NAME, text)
    }

    fun refreshHistory() {
        pubNubManager?.fetchHistory(PubNubManager.CHANNEL_NAME)
    }

    private fun addMessageFromPayload(payload: JsonObject, isHistory: Boolean) {
        val message = ChatMessage(
            text = payload.get("text")?.asString.orEmpty(),
            sender = payload.get("sender")?.asString.orEmpty(),
            timestamp = payload.get("timestamp")?.asString.orEmpty(),
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

    override fun onCleared() {
        super.onCleared()
        pubNubManager?.disconnect()
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val onlineUsers: List<UserPresence> = emptyList(),
    val currentUserId: String? = null,
    val connectionStatus: String = "Connecting",
    val errorMessage: String? = null
)
