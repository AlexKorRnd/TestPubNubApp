package com.example.testpubnubapp

import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.history.PNHistoryResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PubNubManager(
    private val username: String,
    private val onMessageReceived: (JsonObject, Boolean) -> Unit,
    private val onPresenceChange: (String, Boolean) -> Unit,
    private val onStatus: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val pubnub: PubNub

    init {
        val config = PNConfiguration().apply {
            publishKey = PUBLISH_KEY
            subscribeKey = SUBSCRIBE_KEY
            uuid = username
            secure = true
        }
        pubnub = PubNub(config)
        addListener()
    }

    private fun addListener() {
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (status.isError) {
                    onError("${status.category} (${status.errorData.throwable?.message})")
                } else {
                    onStatus(status.category.name)
                }
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                val payload = message.message.asJsonObject
                onMessageReceived(payload, false)
            }

            override fun presence(pubnub: PubNub, presence: PNPresenceEventResult) {
                val uuid = presence.uuid ?: return
                when (presence.event) {
                    "join", "state-change" -> onPresenceChange(uuid, true)
                    "leave", "timeout" -> onPresenceChange(uuid, false)
                }
            }
        })
    }

    fun subscribe(channel: String) {
        pubnub.subscribe(
            channels = listOf(channel),
            withPresence = true
        )
    }

    fun publish(channel: String, text: String) {
        val payload = JsonObject().apply {
            addProperty("text", text)
            addProperty("sender", username)
            addProperty("timestamp", formatTimestamp(System.currentTimeMillis()))
        }
        pubnub.publish(
            channel = channel,
            message = payload
        ).async { result, status ->
            if (status.isError) {
                onError("Publish error: ${status.errorData.throwable?.message}")
            } else {
                onMessageReceived(payload, false)
            }
        }
    }

    fun fetchHistory(channel: String, count: Int = 50) {
        pubnub.history(
            channel = channel,
            count = count
        ).async { result: PNHistoryResult?, status: PNStatus ->
            if (status.isError) {
                onError("History error: ${status.errorData.throwable?.message}")
                return@async
            }
            val messages = result?.messages.orEmpty()
            messages.forEach { entry ->
                entry.entry.asJsonObject?.let { onMessageReceived(it, true) }
            }
        }
    }

    fun hereNow(channel: String) {
        pubnub.hereNow(
            channels = listOf(channel),
            includeUUIDs = true,
            includeState = false
        ).async { result, status ->
            if (status.isError) {
                onError("HereNow error: ${status.errorData.throwable?.message}")
                return@async
            }
            result?.channels?.get(channel)?.occupants?.forEach { occupant ->
                occupant.uuid?.let { onPresenceChange(it, true) }
            }
        }
    }

    fun disconnect() {
        pubnub.unsubscribeAll()
        pubnub.destroy()
    }

    private fun formatTimestamp(epochMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(epochMillis))
    }

    companion object {
        const val CHANNEL_NAME = "global_chat"
        const val PUBLISH_KEY = "demo"
        const val SUBSCRIBE_KEY = "demo"
    }
}
