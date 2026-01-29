package com.example.testpubnubapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import android.net.Uri
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import android.app.Activity
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testpubnubapp.ChatUiState
import com.example.testpubnubapp.PubNubManager

private const val ROUTE_HOME = "home"
private const val ROUTE_MENTIONS = "mentions"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_NEW_CHAT = "new_chat"
private const val ROUTE_GROUP_CHAT = "group_chat"
private const val ROUTE_CHAT = "chat"

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScaffold(
    uiState: ChatUiState,
    onSend: (String, String) -> Unit,
    onMarkChatRead: (String) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val activity = LocalContext.current as? Activity
    val bottomItems = listOf(
        BottomNavItem(ROUTE_HOME, "Home", Icons.Default.Home),
        BottomNavItem(ROUTE_MENTIONS, "Mentions", Icons.Default.AlternateEmail),
        BottomNavItem(ROUTE_PROFILE, "Profile", Icons.Default.Person)
    )
    val showFab = currentDestination?.route == ROUTE_HOME
    fun directChatId(userId: String) = "dm:$userId"
    fun groupChatId(groupName: String) = "group:$groupName"
    fun unreadCountFor(chatId: String): Int {
        val lastRead = uiState.lastReadAt[chatId] ?: 0L
        return uiState.messages.count { message ->
            message.chatId == chatId &&
                message.sender != uiState.currentUserId &&
                message.timestampEpochMillis > lastRead
        }
    }

    val currentRoute = currentDestination?.route
    val chatTitle = backStackEntry?.arguments?.getString("chatTitle").orEmpty()
    val isChatRoute = currentRoute == "$ROUTE_CHAT/{chatId}/{chatTitle}"

    LaunchedEffect(currentRoute, chatTitle) {
        activity?.title = if (isChatRoute) chatTitle else ""
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = { navController.navigate(ROUTE_NEW_CHAT) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New chat")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_HOME) {
                val onlineIds = uiState.onlineUsers.map { it.id }.toSet()
                val directMessageUsers = uiState.messages
                    .map { it.chatId }
                    .filter { it.startsWith("dm:") }
                    .map { it.removePrefix("dm:") }
                    .filter { it.isNotBlank() && it != uiState.currentUserId }
                    .distinct()
                val dmItems = directMessageUsers.map { sender ->
                    DirectMessageItem(
                        id = directChatId(sender),
                        name = sender,
                        isOnline = onlineIds.contains(sender)
                    )
                }
                val publicChannels = listOf(
                    ChannelItem(
                        id = PubNubManager.CHANNEL_NAME,
                        name = "#${PubNubManager.CHANNEL_NAME}",
                        messageCount = uiState.messages.count {
                            it.chatId == PubNubManager.CHANNEL_NAME
                        }
                    )
                )
                val groups = emptyList<GroupItem>()
                val unreadItems = buildList {
                    publicChannels.forEach { channel ->
                        val count = unreadCountFor(channel.id)
                        if (count > 0) {
                            add(
                                UnreadItem(
                                    id = channel.id,
                                    name = channel.name,
                                    count = count
                                )
                            )
                        }
                    }
                    dmItems.forEach { dmItem ->
                        val count = unreadCountFor(dmItem.id)
                        if (count > 0) {
                            add(
                                UnreadItem(
                                    id = dmItem.id,
                                    name = dmItem.name,
                                    count = count
                                )
                            )
                        }
                    }
                    groups.forEach { group ->
                        val count = unreadCountFor(group.id)
                        if (count > 0) {
                            add(
                                UnreadItem(
                                    id = group.id,
                                    name = group.name,
                                    count = count
                                )
                            )
                        }
                    }
                }.sortedByDescending { it.count }
                HomeScreen(
                    userName = uiState.currentUserId?.takeIf { it.isNotBlank() } ?: "User",
                    unreadItems = unreadItems,
                    publicChannels = publicChannels,
                    groups = groups,
                    dmItems = dmItems,
                    onChatSelected = { chatId, chatName ->
                        navController.navigate(
                            "$ROUTE_CHAT/${Uri.encode(chatId)}/${Uri.encode(chatName)}"
                        )
                    }
                )
            }
            composable(ROUTE_MENTIONS) {
                PlaceholderScreen(
                    title = "Mentions",
                    description = "Mentions will appear here."
                )
            }
            composable(ROUTE_PROFILE) {
                PlaceholderScreen(
                    title = "Profile",
                    description = "Profile details will appear here."
                )
            }
            composable(ROUTE_NEW_CHAT) {
                NewChatScreen(onStartGroupChat = {
                    val groupName = "Group chat"
                    navController.navigate(
                        "$ROUTE_CHAT/${Uri.encode(groupChatId(groupName))}/${Uri.encode(groupName)}"
                    )
                })
            }
            composable(ROUTE_GROUP_CHAT) {
                ChatScreen(
                    uiState = uiState,
                    chatId = groupChatId("Group chat"),
                    onSend = onSend,
                    onChatOpened = onMarkChatRead
                )
            }
            composable("$ROUTE_CHAT/{chatId}/{chatTitle}") { entry ->
                val chatId = entry.arguments?.getString("chatId").orEmpty()
                val chatTitle = entry.arguments?.getString("chatTitle").orEmpty()
                ChatScreen(
                    uiState = uiState,
                    chatId = chatId.ifBlank { PubNubManager.CHANNEL_NAME },
                    onSend = onSend,
                    onChatOpened = onMarkChatRead
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(text = description, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun NewChatScreen(onStartGroupChat: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "New chat", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Start a new conversation or open a group chat.")
            androidx.compose.material3.Button(onClick = onStartGroupChat) {
                Text(text = "Open group chat")
            }
        }
    }
}
