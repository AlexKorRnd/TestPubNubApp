package com.example.testpubnubapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testpubnubapp.ChatUiState

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onSend: (String) -> Unit,
    onRefreshHistory: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Status: ${uiState.connectionStatus}",
            style = MaterialTheme.typography.bodyMedium
        )
        uiState.errorMessage?.let { error ->
            Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Online Users (${uiState.onlineUsers.size})")
            Button(onClick = onRefreshHistory) {
                Text("Refresh History")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            items(uiState.onlineUsers) { user ->
                Text(text = "• ${user.id}")
            }
        }

        Text(text = "Messages", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages) { message ->
                Column {
                    Text(text = "${message.sender} @ ${message.timestamp}")
                    Text(text = message.text)
                    if (message.isHistory) {
                        Text(
                            text = "(history)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Message") }
            )
            Button(
                onClick = {
                    onSend(messageText)
                    messageText = ""
                },
                enabled = messageText.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}
