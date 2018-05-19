package me.ialistannen.livingparchment.backend.di

import dagger.Module
import dagger.Provides
import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.fetching.amazon.AmazonFetcher
import me.ialistannen.livingparchment.backend.fetching.combined.then
import me.ialistannen.livingparchment.backend.fetching.goodreads.GoodReadFetcher

@Module
class FetchingModule {

    @Provides
    @ApplicationScope
    fun provideFetchingModule(): BookFetcher = GoodReadFetcher().then(AmazonFetcher())
}