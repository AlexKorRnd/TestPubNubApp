package com.example.testpubnubapp.ui

import com.example.testpubnubapp.models.ChatMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ChatMessage.toLocalDate(): LocalDate {
    val zoneId = ZoneId.systemDefault()
    return Instant.ofEpochMilli(timestampEpochMillis).atZone(zoneId).toLocalDate()
}

fun formatMessageTimestamp(epochMillis: Long): String {
    val zoneId = ZoneId.systemDefault()
    val locale = Locale.getDefault()
    val messageDateTime = Instant.ofEpochMilli(epochMillis).atZone(zoneId)
    val messageDate = messageDateTime.toLocalDate()
    val today = LocalDate.now(zoneId)
    return when {
        messageDate == today -> messageDateTime.format(DateTimeFormatter.ofPattern("HH:mm", locale))
        messageDate == today.minusDays(1) -> {
            val time = messageDateTime.format(DateTimeFormatter.ofPattern("HH:mm", locale))
            "Вчера, $time"
        }
        messageDate.year == today.year -> messageDateTime.format(
            DateTimeFormatter.ofPattern("d MMM, HH:mm", locale)
        )
        else -> messageDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", locale))
    }
}

fun formatDateSeparator(date: LocalDate): String {
    val zoneId = ZoneId.systemDefault()
    val locale = Locale.getDefault()
    val today = LocalDate.now(zoneId)
    return when {
        date == today -> "Сегодня"
        date == today.minusDays(1) -> "Вчера"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
    }
}
