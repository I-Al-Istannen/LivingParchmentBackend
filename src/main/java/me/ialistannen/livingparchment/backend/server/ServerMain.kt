package me.ialistannen.livingparchment.backend.server

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter
import io.dropwizard.Application
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import kotlinx.coroutines.experimental.runBlocking
import me.ialistannen.livingparchment.backend.di.DaggerBackendMainComponent
import me.ialistannen.livingparchment.backend.server.auth.JwtAuthenticator
import me.ialistannen.livingparchment.backend.server.auth.User
import me.ialistannen.livingparchment.backend.server.config.LivingParchmentConfiguration
import me.ialistannen.livingparchment.backend.server.database.ManagedJdbi
import me.ialistannen.livingparchment.backend.server.health.DatabaseHealthCheck
import me.ialistannen.livingparchment.backend.server.resources.*
import me.ialistannen.livingparchment.backend.storage.UserRepository
import me.ialistannen.livingparchment.backend.storage.sql.DatabaseCreator
import org.eclipse.jetty.util.component.AbstractLifeCycle
import org.eclipse.jetty.util.component.LifeCycle
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.HmacKey
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

class ServerMain : Application<LivingParchmentConfiguration>() {

    @Inject
    lateinit var bookEndpoint: BookAddEndpoint
    @Inject
    lateinit var bookQueryEndpoint: BookQueryEndpoint
    @Inject
    lateinit var bookDeleteEndpoint: BookDeleteEndpoint
    @Inject
    lateinit var bookLocationEndpoint: BookLocationEndpoint
    @Inject
    lateinit var userLoginEndpoint: UserLoginEndpoint

    @Inject
    lateinit var jwtAuthenticator: JwtAuthenticator

    @Inject
    lateinit var userRepository: UserRepository

    override fun getName(): String {
        return "living-parchment"
    }

    override fun run(configuration: LivingParchmentConfiguration, environment: Environment) {
        val managedJdbi = ManagedJdbi(
                connectionString = configuration.dbConnectionString,
                password = configuration.dbPassword,
                user = configuration.dbUser
        )
        environment.lifecycle().manage(managedJdbi)
        environment.healthChecks()
                .register("database-health", DatabaseHealthCheck(managedJdbi.getJdbi()))

        DaggerBackendMainComponent.builder()
                .jdbi(managedJdbi)
                .jwtKey(HmacKey(configuration.getJwtTokenSecret()))
                .build()
                .inject(this)

        environment.jersey().register(bookEndpoint)
        environment.jersey().register(bookQueryEndpoint)
        environment.jersey().register(bookDeleteEndpoint)
        environment.jersey().register(bookLocationEndpoint)
        environment.jersey().register(userLoginEndpoint)
        environment.jersey().register(HttpMethodOverrideFilter())

        registerAuthentication(configuration, environment)

        environment.lifecycle()
                .addLifeCycleListener(object : AbstractLifeCycle.AbstractLifeCycleListener() {
                    override fun lifeCycleStarted(event: LifeCycle) {
                        managedJdbi.getJdbi().useHandle<RuntimeException> {
                            DatabaseCreator().createTables(it)
                        }
                        runBlocking {
                            userRepository.addUser(User("Al", BCrypt.hashpw(
                                    "123456", BCrypt.gensalt()
                            )))
                        }
                    }
                })
    }

    private fun registerAuthentication(configuration: LivingParchmentConfiguration, environment: Environment) {
        val jwtConsumer = JwtConsumerBuilder()
                .setRequireSubject()
                .setAllowedClockSkewInSeconds(30)
                .setRequireExpirationTime()
                .setVerificationKey(HmacKey(configuration.getJwtTokenSecret()))
                .build()

        environment.jersey().register(AuthDynamicFeature(
                JwtAuthFilter.Builder<User>()
                        .setJwtConsumer(jwtConsumer)
                        .setRealm("LivingParchment server")
                        .setPrefix("Bearer")
                        .setAuthenticator(jwtAuthenticator)
                        .buildAuthFilter()
        ))
    }

    override fun initialize(bootstrap: Bootstrap<LivingParchmentConfiguration>) {
        bootstrap.objectMapper.registerModule(KotlinModule())
    }
}

fun main(args: Array<String>) {
    ServerMain().run(
            "server",
            ServerMain::class.java.getResource("/living-parchment.yml")
                    .toString()
                    .removePrefix("file:")
    )
}