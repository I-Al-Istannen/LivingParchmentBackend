package me.ialistannen.livingparchment.backend.util

import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun Date.toLocalDate(): LocalDate? {
    return toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}
