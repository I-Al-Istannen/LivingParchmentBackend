package me.ialistannen.livingparchment.backend.storage

/**
 * Indicates that something went wrong while storing/retrieving something from the database.
 */
class StorageException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)
}