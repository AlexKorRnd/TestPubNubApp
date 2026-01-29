package com.example.testpubnubapp

import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.history.PNHistoryResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.v2.callbacks.Consumer
import com.pubnub.api.v2.callbacks.Result
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
        val userId = UserId(username)
        pubnub = PubNub.create(userId, SUBSCRIBE_KEY, {
            this.publishKey = PUBLISH_KEY
            this.secure = true
        })
        addListener()
    }

    private fun addListener() {
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (status.error) {
                    onError("${status.category} (${status.exception?.message})")
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
        ).async { result ->
            if (result.isFailure) {
                onError("Publish error: ${result.exceptionOrNull()}")
            }
        }
    }

    fun fetchHistory(channel: String, count: Int = 50) {
        pubnub.history(
            channel = channel,
            count = count
        ).async { result ->
            if (result.isFailure) {
                onError("History error: ${result.exceptionOrNull()}")
                return@async
            }
            val messages = result.getOrNull()?.messages.orEmpty()
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
        ).async { result ->
            if (result.isFailure) {
                onError("HereNow error: ${result.exceptionOrNull()}")
                return@async
            }
            result.getOrNull()?.channels?.get(channel)?.occupants?.forEach { occupant ->
                occupant.uuid.let { onPresenceChange(it, true) }
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
        const val PUBLISH_KEY = "pub-c-1275d4de-a88d-46ef-977e-1b836aaf9d99"
        const val SUBSCRIBE_KEY = "sub-c-9ed5200e-667c-4004-b00e-bd643f325a22"
    }
}
