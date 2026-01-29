package com.example.testpubnubapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import com.example.testpubnubapp.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val launchChat: (String) -> Unit = { username ->
                    startActivity(
                        Intent(this, ChatActivity::class.java).putExtra(
                            ChatActivity.EXTRA_USERNAME,
                            username
                        )
                    )
                }
                LoginScreen(onConnect = launchChat)
            }
        }
    }
}
