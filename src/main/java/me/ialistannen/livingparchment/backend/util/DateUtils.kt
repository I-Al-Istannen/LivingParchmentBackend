package me.ialistannen.livingparchment.backend.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun Date.toLocalDate(): LocalDate? {
    return toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

fun LocalDate.toDate(): Date {
    return Date(atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
}