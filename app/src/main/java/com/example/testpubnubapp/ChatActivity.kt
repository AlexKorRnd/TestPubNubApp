package com.example.testpubnubapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testpubnubapp.ui.MainScaffold

class ChatActivity : ComponentActivity() {
    private val viewModel: MessageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra(EXTRA_USERNAME).orEmpty()

        setContent {
            MaterialTheme {
                LaunchedEffect(username) {
                    if (username.isNotBlank()) {
                        viewModel.connect(username)
                    }
                }
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                MainScaffold(
                    uiState = uiState,
                    onSend = viewModel::sendMessage,
                    onRefreshHistory = viewModel::refreshHistory
                )
            }
        }
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }
}
