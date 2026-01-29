package com.example.testpubnubapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.testpubnubapp.ChatUiState

private const val ROUTE_HOME = "home"
private const val ROUTE_MENTIONS = "mentions"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_NEW_CHAT = "new_chat"
private const val ROUTE_GROUP_CHAT = "group_chat"

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScaffold(
    uiState: ChatUiState,
    onSend: (String) -> Unit,
    onRefreshHistory: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomItems = listOf(
        BottomNavItem(ROUTE_HOME, "Home", Icons.Default.Home),
        BottomNavItem(ROUTE_MENTIONS, "Mentions", Icons.Default.AlternateEmail),
        BottomNavItem(ROUTE_PROFILE, "Profile", Icons.Default.Person)
    )
    val showFab = currentDestination?.route != ROUTE_NEW_CHAT

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(ROUTE_HOME) { saveState = true }
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
                ChatScreen(
                    uiState = uiState,
                    onSend = onSend,
                    onRefreshHistory = onRefreshHistory
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
                NewChatScreen(onStartGroupChat = { navController.navigate(ROUTE_GROUP_CHAT) })
            }
            composable(ROUTE_GROUP_CHAT) {
                ChatScreen(
                    uiState = uiState,
                    onSend = onSend,
                    onRefreshHistory = onRefreshHistory
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
