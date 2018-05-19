package me.ialistannen.livingparchment.backend.fetching.amazon

import me.ialistannen.livingparchment.backend.fetching.BookFetcher
import me.ialistannen.livingparchment.backend.fetching.FetcherTest

internal class AmazonFetcherTest : FetcherTest() {

    override val fetcher: BookFetcher = AmazonFetcher()
}