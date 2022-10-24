package com.konovus.apitesting.data.local.entities

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class ChartData(
    val timestamp: String,
    val close: Double,
    val lastUpdatedTime: Long = System.currentTimeMillis()
) {

    fun toLocalDateTime(): LocalDateTime {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return LocalDateTime.parse(timestamp, formatter)
    }

    fun toLocalDate(): LocalDate {
        val pattern = "yyyy-MM-dd"
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return LocalDate.parse(timestamp, formatter)
    }

}
