package com.example.coursemate.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val monthDayFormatter = DateTimeFormatter.ofPattern("M月d日")
private val monthDayTimeFormatter = DateTimeFormatter.ofPattern("M月d日 HH:mm")
private val fullDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm")

fun parseDateTime(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) {
        return null
    }
    return runCatching { OffsetDateTime.parse(value).toLocalDateTime() }
        .recoverCatching { LocalDateTime.parse(value) }
        .getOrNull()
}

fun formatDateTime(value: String?): String {
    return parseDateTime(value)?.format(fullDateTimeFormatter) ?: "时间未知"
}

fun formatShortDateTime(value: String?): String {
    return parseDateTime(value)?.format(monthDayTimeFormatter) ?: "时间未知"
}

fun formatShortDate(value: String?): String {
    return parseDateTime(value)?.format(monthDayFormatter) ?: "未设置"
}

fun formatRelativeTime(value: String?): String {
    val target = parseDateTime(value) ?: return "刚刚"
    val now = LocalDateTime.now()
    val duration = Duration.between(target, now)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes} 分钟前"
        hours < 24 -> "${hours} 小时前"
        days < 7 -> "${days} 天前"
        else -> target.format(monthDayFormatter)
    }
}

fun initialsOf(value: String): String {
    val parts = value
        .trim()
        .split(" ", "·")
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) {
        return "CM"
    }
    return parts.take(2).joinToString("") { part ->
        part.first().uppercase()
    }
}
