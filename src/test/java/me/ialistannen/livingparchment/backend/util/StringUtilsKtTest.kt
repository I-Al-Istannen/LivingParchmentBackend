package me.ialistannen.livingparchment.backend.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StringUtilsKtTest {

    @Test
    fun `snake case one word`() {
        assertEquals("word", "word".camelToSnakeCase())
    }

    @Test
    fun `snake case one capitalized word`() {
        assertEquals("word", "Word".camelToSnakeCase())
    }

    @Test
    fun `snake case two words`() {
        assertEquals("word_two", "wordTwo".camelToSnakeCase())
    }
}