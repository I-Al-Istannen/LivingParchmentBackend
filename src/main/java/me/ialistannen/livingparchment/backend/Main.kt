package me.ialistannen.livingparchment.backend

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.common.api.query.QueryType
import me.ialistannen.livingparchment.common.model.Book
import java.util.*

fun main(args: Array<String>) {
    println("YAY")

    val mainComponent = DaggerBackendMainComponent.create()

    println(mainComponent.getBookRepository()::class.java)

    val bookRepository = mainComponent.getBookRepository()

    val book = Book(
            "Drachenreiter",
            200,
            "123456",
            "German",
            Date(),
            authors = listOf("Cornelia", "Funke"),
            genre = listOf("Fantasy", "Children")
    )

    runBlocking {
        bookRepository.addBook(book)

        bookRepository.getAllBooks().forEach { println(it) }

        println("For query exact match")
        bookRepository.getBooksForQuery(
                QueryType.EXACT_MATCH, "isbn", "123456"
        ).forEach { println(it) }

        println("For query part")
        bookRepository.getBooksForQuery(
                QueryType.PART, "isbn", "34"
        ).forEach { println(it) }

        println("For query regex")
        bookRepository.getBooksForQuery(
                QueryType.REGEX_MATCH, "isbn", "[\\d]+"
        ).forEach { println(it) }

        println()
        println("==== JSON ====")
        println()
        println("For query exact match json")
        bookRepository.getBooksForQuery(
                QueryType.EXACT_MATCH, "authors", "Cornelia"
        ).forEach { println(it) }

        println("For query part json")
        bookRepository.getBooksForQuery(
                QueryType.PART, "authors", "Cornelia"
        ).forEach { println(it) }

        println("For query regex json")
        bookRepository.getBooksForQuery(
                QueryType.REGEX_MATCH, "authors", ".+Corne.+"
        ).forEach { println(it) }
    }
}