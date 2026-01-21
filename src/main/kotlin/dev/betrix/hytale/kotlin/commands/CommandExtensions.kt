@file:JvmName("CommandExtensions")

package dev.betrix.hytale.kotlin.commands

import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg

/**
 * Gets the value of an argument, returning null if not provided.
 * This is a more Kotlin-idiomatic way to access optional arguments.
 *
 * Example:
 * ```kotlin
 * val gameMode: String? = gameModeArg.getOrNull(context)
 * if (gameMode == null) {
 *     context.msg("Usage: /queue <mode>")
 *     return
 * }
 * ```
 *
 * @param context The command context
 * @return The argument value or null if not provided
 */
public fun <T> Argument<*, T>.getOrNull(context: CommandContext): T? =
    if (context.provided(this)) get(context) else null

/**
 * Gets the value of an argument or a default value if not provided.
 *
 * Example:
 * ```kotlin
 * val count: Int = countArg.getOrDefault(context, 1)
 * ```
 *
 * @param context The command context
 * @param default The default value to return if not provided
 * @return The argument value or the default
 */
public fun <T> Argument<*, T>.getOrDefault(context: CommandContext, default: T): T =
    if (context.provided(this)) get(context) else default

/**
 * Checks if this argument was provided in the command context.
 *
 * Example:
 * ```kotlin
 * if (debugArg.wasProvided(context)) {
 *     enableDebugMode()
 * }
 * ```
 *
 * @param context The command context
 * @return true if the argument was provided
 */
public fun Argument<*, *>.wasProvided(context: CommandContext): Boolean =
    context.provided(this)

/**
 * Gets the raw input strings for this argument.
 *
 * @param context The command context
 * @return The input strings or null if not provided
 */
public fun Argument<*, *>.getInput(context: CommandContext): Array<String>? =
    context.getInput(this)

/**
 * Executes a block if this argument was provided.
 *
 * Example:
 * ```kotlin
 * radiusArg.ifProvided(context) { radius ->
 *     setSearchRadius(radius)
 * }
 * ```
 *
 * @param context The command context
 * @param block The block to execute with the argument value
 */
public inline fun <T> Argument<*, T>.ifProvided(context: CommandContext, block: (T) -> Unit) {
    if (context.provided(this)) {
        block(get(context))
    }
}

/**
 * Transforms the argument value if provided, otherwise returns null.
 *
 * Example:
 * ```kotlin
 * val player: Player? = playerNameArg.mapIfProvided(context) { name ->
 *     findPlayerByName(name)
 * }
 * ```
 *
 * @param context The command context
 * @param transform The transformation function
 * @return The transformed value or null if not provided
 */
public inline fun <T, R> Argument<*, T>.mapIfProvided(
    context: CommandContext,
    transform: (T) -> R
): R? = if (context.provided(this)) transform(get(context)) else null

/**
 * Gets the argument value and throws if not provided.
 * Use this for arguments that should always be present.
 *
 * @param context The command context
 * @return The argument value
 * @throws IllegalStateException if the argument was not provided
 */
public fun <T> Argument<*, T>.require(context: CommandContext): T =
    if (context.provided(this)) {
        get(context)
    } else {
        throw IllegalStateException("Required argument '${name}' was not provided")
    }
