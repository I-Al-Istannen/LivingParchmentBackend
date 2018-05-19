package me.ialistannen.livingparchment.backend

import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.backend.fetching.amazon.AmazonFetcher
import me.ialistannen.livingparchment.backend.fetching.combined.then
import me.ialistannen.livingparchment.backend.fetching.goodreads.GoodReadFetcher
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

        println("==== All ====")
        println()
        bookRepository.getAllBooks().forEach { println(it) }

        println()
        println("==== Field ====")
        println()

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

    println()
    println()
    println()
    runBlocking {
        println(GoodReadFetcher().then(AmazonFetcher()).then(GoodReadFetcher()))
        println(GoodReadFetcher().fetch("9780439321617"))
        println(GoodReadFetcher().fetch("1596063084"))
    }
}