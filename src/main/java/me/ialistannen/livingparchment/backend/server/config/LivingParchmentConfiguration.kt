package me.ialistannen.livingparchment.backend.server.config

import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

class LivingParchmentConfiguration : Configuration() {

    @NotEmpty
    lateinit var dbUser: String

    @NotEmpty
    lateinit var dbConnectionString: String

    @NotEmpty
    lateinit var dbPassword: String

    @NotEmpty
    lateinit var jwtTokenSecret: String

    /**
     * Returns the jwt client secret used to authenticate users.
     */
    fun getJwtTokenSecret(): ByteArray {
        return jwtTokenSecret.toByteArray()
    }
}