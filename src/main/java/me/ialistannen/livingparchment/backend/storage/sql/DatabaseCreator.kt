package me.ialistannen.livingparchment.backend.storage.sql

import org.jdbi.v3.core.Handle

class DatabaseCreator {

    /**
     * Creates all relevant tables, if needed.
     */
    fun createTables(handle: Handle) {
        handle.createUpdate("""
            CREATE TABLE IF NOT EXISTS BookLocations (
              id          UUID        NOT NULL PRIMARY KEY,
              name        VARCHAR(40) NOT NULL UNIQUE,
              description TEXT DEFAULT ''
            );""".trimIndent()
        ).execute()

        handle.createUpdate("""
            CREATE TABLE IF NOT EXISTS Books (
              isbn       VARCHAR(13) PRIMARY KEY,
              title      TEXT NOT NULL,
              language   VARCHAR(20),
              page_count INTEGER,
              publisher  TEXT,
              published  DATE,
              location   UUID NULL REFERENCES BookLocations(id) ON UPDATE CASCADE ON DELETE RESTRICT,
              extra      JSONB
            );""".trimIndent()
        ).execute()
    }
}