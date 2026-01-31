package com.example.testpubnubapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.testpubnubapp.ChatUiState
import com.example.testpubnubapp.models.ChatMessage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    chatId: String,
    onSend: (String, String) -> Unit,
    onChatOpened: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messageListState = rememberLazyListState()
    val visibleMessages = uiState.messages.filter { it.chatId == chatId }

    LaunchedEffect(chatId, uiState.messages.size) {
        onChatOpened(chatId)
        if (visibleMessages.isNotEmpty()) {
            messageListState.animateScrollToItem(visibleMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = messageListState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(visibleMessages) { index, message ->
                val currentDate = message.toLocalDate()
                val previousDate = visibleMessages.getOrNull(index - 1)?.toLocalDate()
                if (previousDate == null || previousDate != currentDate) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formatDateSeparator(currentDate),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                val isCurrentUser = message.sender == uiState.currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
                    ) {
                        if (!isCurrentUser) {
                            Text(
                                text = message.sender,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentUser) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = message.text)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatMessageTimestamp(message.timestampEpochMillis),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    if (message.isHistory) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        AssistChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = "history",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            enabled = false,
                                            colors = AssistChipDefaults.assistChipColors(
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: attach files */ }) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file"
                )
            }
            IconButton(onClick = { /* TODO: attach photo */ }) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Attach photo"
                )
            }
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Напишите сообщение…") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            IconButton(
                onClick = {
                    onSend(chatId, messageText)
                    messageText = ""
                },
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}
