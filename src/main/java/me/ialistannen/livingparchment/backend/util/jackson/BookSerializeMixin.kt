package me.ialistannen.livingparchment.backend.util.jackson

import com.fasterxml.jackson.annotation.JsonIgnore

abstract class BookSerializeMixin {

    @JsonIgnore
    abstract fun getAuthors(): List<String>

    @JsonIgnore
    abstract fun getGenre(): List<String>
}