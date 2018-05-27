package me.ialistannen.livingparchment.backend.storage.sql

import com.google.gson.JsonElement
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.argument.ArgumentFactory
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.statement.StatementContext
import org.postgresql.util.PGobject
import java.lang.reflect.Type
import java.sql.PreparedStatement
import java.util.*

class JsonNNodeArgumentFactory : ArgumentFactory {

    class JsonNodeArgument(private val value: PGobject) : Argument {

        override fun apply(position: Int, statement: PreparedStatement, ctx: StatementContext) {
            statement.setObject(position, value)
        }
    }

    override fun build(type: Type, value: Any, config: ConfigRegistry): Optional<Argument> {
        if (value !is JsonElement) {
            return Optional.empty()
        }

        val postgresObject = PGobject()
        postgresObject.type = "jsonb"
        postgresObject.value = value.toString()

        return Optional.of(JsonNodeArgument(postgresObject))
    }

}