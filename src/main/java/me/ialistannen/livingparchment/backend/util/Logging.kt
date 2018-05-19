package me.ialistannen.livingparchment.backend.util

import java.util.logging.Logger

fun <T : Any> T.logger(): Lazy<Logger> {
    return lazy { Logger.getLogger(this.javaClass.name) }
}