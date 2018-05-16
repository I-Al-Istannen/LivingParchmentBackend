package me.ialistannen.livingparchment.backend

import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
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

    bookRepository.addBook(book)
}