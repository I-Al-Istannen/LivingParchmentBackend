package me.ialistannen.livingparchment.backend.fetching.goodreads

import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.fetching.FetcherTest

internal class GoodReadFetcherTest : FetcherTest() {
    override val fetcher: BookFetcher = GoodReadFetcher()
}