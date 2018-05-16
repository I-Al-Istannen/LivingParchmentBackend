package me.ialistannen.livingparchment.backend.util


private val snakeCaseRegex = Regex("([A-Z])")

/**
 * Converts a String from camelCase to snake_case.
 *
 * @return the snake_case string
 */
fun String.camelToSnakeCase(): String {
    return decapitalize().replace(snakeCaseRegex, "_$1").toLowerCase()
}