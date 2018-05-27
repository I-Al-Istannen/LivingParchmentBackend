package me.ialistannen.livingparchment.backend.di

import dagger.Binds
import dagger.Module
import me.ialistannen.livingparchment.backend.storage.BookRepository
import me.ialistannen.livingparchment.backend.storage.sql.book.SqlBookRepository

@Module
abstract class BookRepositoryModule {

    @ApplicationScope
    @Binds
    abstract fun bookRepository(sqlBookRepository: SqlBookRepository): BookRepository
}