package com.dselivetracker.utils

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val bdZone = ZoneId.of("Asia/Dhaka")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")

    fun calculateSettlementDate(timestamp: Long, category: String): LocalDate? {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), bdZone).toLocalDate()
        val workingDays = when (category.uppercase()) {
            "A", "B" -> 2
            "Z" -> 3
            else -> return null
        }
        var current = date
        var remaining = workingDays
        while (remaining > 0) {
            current = current.plusDays(1)
            if (current.dayOfWeek != DayOfWeek.FRIDAY && current.dayOfWeek != DayOfWeek.SATURDAY) {
                remaining--
            }
        }
        return current
    }

    fun formatSettlementDate(timestamp: Long, category: String): String? {
        val settlement = calculateSettlementDate(timestamp, category) ?: return null
        return settlement.format(dateFormatter) + " (${settlement.format(dayFormatter)})"
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), bdZone)
        return date.format(dateFormatter)
    }
}
