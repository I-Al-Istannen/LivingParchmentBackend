package me.ialistannen.livingparchment.backend.storage.sql

import me.ialistannen.livingparchment.backend.storage.StorageException
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi

/**
 * Performs an action and wraps any resulting exception in a [StorageException] instance.
 *
 * @param consumer the consumer for the handle
 */
fun Jdbi.using(consumer: Handle.() -> Unit) {
    useHandle<StorageException> {
        try {
            it.consumer()
        } catch (e: Exception) {
            throw StorageException(e)
        }
    }
}