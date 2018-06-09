package me.ialistannen.livingparchment.backend.server.resources

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.dropwizard.testing.junit5.ResourceExtension
import me.ialistannen.livingparchment.backend.util.jackson.BookSerializeMixin
import me.ialistannen.livingparchment.common.model.Book
import org.glassfish.jersey.client.ClientProperties
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.Form
import javax.ws.rs.core.MultivaluedHashMap
import javax.ws.rs.core.Response

abstract class ResourceTest {

    protected abstract val endpoint: Any

    protected abstract val extension: ResourceExtension

    protected abstract val path: String

    /**
     * Creates the test extension for the endpoint.
     */
    protected fun extension(): ResourceExtension =
            ResourceExtension.builder()
                    .addResource(endpoint)
                    .configureBasics()
                    .build()

    private fun ResourceExtension.Builder.configureBasics(): ResourceExtension.Builder {
        return setMapper(
                jacksonObjectMapper()
                        .registerModule(KotlinModule())
                        .addMixIn(Book::class.java, BookSerializeMixin::class.java)
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        )
                .setClientConfigurator {
                    it.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                }
    }

    /**
     * Makes a test call.
     *
     * @param resourceExtension the [ResourceExtension] to use
     * @param path the path to make the request to
     * @param action the call to make
     * @return the result of the call
     */
    protected inline fun <reified T> makeCall(resourceExtension: ResourceExtension, path: String,
                                              queryParams: List<Pair<String, String>> = emptyList(),
                                              action: Invocation.Builder .() -> Response): T {
        var target = resourceExtension.target(path)

        queryParams.forEach { target = target.queryParam(it.first, it.second) }

        return target
                .request()
                .action()
                .readEntity(T::class.java)
    }

    /**
     * Makes a test call.
     *
     * @param action the call to make
     * @return the result of the call
     */
    protected inline fun <reified T> makeCall(queryParams: List<Pair<String, String>> = emptyList(),
                                              action: Invocation.Builder.() -> Response): T {
        return makeCall(extension, path, queryParams, action)
    }

    /**
     * Creates a Form entity based on the entries you pass it.e
     *
     * @param entries the entries to use in the urlencoded form entity
     */
    fun form(vararg entries: Pair<String, String>): Entity<Form>? {
        return Entity.form(MultivaluedHashMap(entries.toMap()))
    }
}