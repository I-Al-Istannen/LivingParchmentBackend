package me.ialistannen.livingparchment.backend.fetching

class FetchException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}