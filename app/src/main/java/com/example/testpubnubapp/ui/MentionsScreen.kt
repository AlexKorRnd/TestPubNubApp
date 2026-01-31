package com.example.testpubnubapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class MentionItem(
    val sender: String,
    val chatId: String,
    val chatName: String,
    val text: String,
    val timestampEpochMillis: Long
)

@Composable
fun MentionsScreen(
    mentions: List<MentionItem>,
    onMentionClick: (MentionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mentions.isEmpty()) {
        PlaceholderScreen(
            title = "Mentions",
            description = "When someone mentions you, you’ll see it here."
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mentions) { mention ->
            Card(
                modifier = Modifier.clickable { onMentionClick(mention) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${mention.sender} · ${mention.chatName}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = mention.text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = formatMessageTimestamp(mention.timestampEpochMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
