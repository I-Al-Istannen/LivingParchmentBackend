package me.ialistannen.livingparchment.backend

import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.common.model.Book

fun main(args: Array<String>) {
    println("YAY")

    val mainComponent = DaggerBackendMainComponent.create()

    println(mainComponent.getBookRepository()::class.java)

    val bookRepository = mainComponent.getBookRepository()

    val book = Book("Drachenreiter", listOf("hm"), 20, "123456", "de")

    bookRepository.addBook(book)
}