package com.example.testpubnubapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class UnreadItem(
    val id: String,
    val name: String,
    val count: Int
)

data class ChannelItem(
    val id: String,
    val name: String,
    val messageCount: Int
)

data class GroupItem(
    val id: String,
    val name: String,
    val memberCount: Int
)

data class DirectMessageItem(
    val id: String,
    val name: String,
    val isOnline: Boolean
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Jane Smith",
    unreadItems: List<UnreadItem> = emptyList(),
    publicChannels: List<ChannelItem> = emptyList(),
    groups: List<GroupItem> = emptyList(),
    dmItems: List<DirectMessageItem> = listOf(
        DirectMessageItem("1", "Cody Fisher", true),
        DirectMessageItem("2", "Ralph Edwards", false),
        DirectMessageItem("3", "Darlene Robertson", true),
        DirectMessageItem("4", "Leslie Alexander", false)
    )
) {
    var searchValue by remember { mutableStateOf("") }
    var unreadExpanded by remember { mutableStateOf(true) }
    var publicExpanded by remember { mutableStateOf(true) }
    var groupsExpanded by remember { mutableStateOf(true) }
    var directExpanded by remember { mutableStateOf(true) }
    val horizontalPadding = 20.dp
    val sectionSpacing = 20.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        TopBar(
            userName = userName,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

        SearchRow(
            value = searchValue,
            onValueChange = { searchValue = it },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

        SectionBlock(
            title = "Unread",
            expanded = unreadExpanded,
            onToggle = { unreadExpanded = !unreadExpanded },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        ) {
            if (unreadItems.isEmpty()) {
                EmptySectionRow(label = "No unread messages")
            } else {
                unreadItems.forEach { item ->
                    SectionItemRow(
                        title = item.name,
                        subtitle = "${item.count} new messages"
                    )
                }
            }
        }

        SectionBlock(
            title = "Public Channels",
            expanded = publicExpanded,
            onToggle = { publicExpanded = !publicExpanded },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        ) {
            if (publicChannels.isEmpty()) {
                EmptySectionRow(label = "No public channels yet")
            } else {
                publicChannels.forEach { channel ->
                    SectionItemRow(
                        title = channel.name,
                        subtitle = "${channel.messageCount} messages"
                    )
                }
            }
        }

        SectionBlock(
            title = "Groups",
            expanded = groupsExpanded,
            onToggle = { groupsExpanded = !groupsExpanded },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        ) {
            if (groups.isEmpty()) {
                EmptySectionRow(label = "No groups yet")
            } else {
                groups.forEach { group ->
                    SectionItemRow(
                        title = group.name,
                        subtitle = "${group.memberCount} members"
                    )
                }
            }
        }

        SectionBlock(
            title = "Direct Messages",
            expanded = directExpanded,
            onToggle = { directExpanded = !directExpanded },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        ) {
            if (dmItems.isEmpty()) {
                EmptySectionRow(label = "No direct messages yet")
            } else {
                dmItems.forEach { item ->
                    DirectMessageRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    userName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchRow(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = title,
            expanded = expanded,
            onToggle = onToggle
        )
        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SectionItemRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySectionRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DirectMessageRow(
    item: DirectMessageItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (item.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ECC71))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
