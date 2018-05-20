package me.ialistannen.livingparchment.backend.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun <T : Any> T.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass.name) }
}