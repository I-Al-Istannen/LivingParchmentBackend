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
}