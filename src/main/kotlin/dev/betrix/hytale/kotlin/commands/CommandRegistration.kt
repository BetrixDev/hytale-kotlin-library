@file:JvmName("CommandRegistration")

package dev.betrix.hytale.kotlin.commands

import com.hypixel.hytale.server.core.command.system.AbstractCommand
import com.hypixel.hytale.server.core.plugin.JavaPlugin

/**
 * Registers a command with the plugin's command registry.
 *
 * Example:
 * ```kotlin
 * registerCommand(hubCommand)
 * ```
 *
 * @param command The command to register
 */
public fun JavaPlugin.registerCommand(command: AbstractCommand) {
    commandRegistry.registerCommand(command)
}

/**
 * Registers multiple commands with the plugin's command registry.
 *
 * Example:
 * ```kotlin
 * registerCommands(
 *     hubCommand,
 *     queueCommand,
 *     kitCommand
 * )
 * ```
 *
 * @param commands The commands to register
 */
public fun JavaPlugin.registerCommands(vararg commands: AbstractCommand) {
    commands.forEach { commandRegistry.registerCommand(it) }
}

/**
 * Registers commands using a configuration block with DSL.
 *
 * Example:
 * ```kotlin
 * registerCommands {
 *     +hubCommand
 *     +queueCommand
 *
 *     +playerCommand("test", "Test command") {
 *         execute { player, context ->
 *             context.msg("Hello!")
 *         }
 *     }
 * }
 * ```
 *
 * @param block The configuration block
 */
public inline fun JavaPlugin.registerCommands(block: CommandRegistrationScope.() -> Unit) {
    CommandRegistrationScope(this).apply(block)
}

/**
 * Scope for registering multiple commands with a DSL.
 */
public class CommandRegistrationScope(
    @PublishedApi internal val plugin: JavaPlugin
) {
    /**
     * Registers a command using the unary plus operator.
     *
     * @param command The command to register
     * @return The registered command
     */
    public operator fun <C : AbstractCommand> C.unaryPlus(): C {
        plugin.commandRegistry.registerCommand(this)
        return this
    }

    /**
     * Registers a command.
     *
     * @param command The command to register
     * @return The registered command
     */
    public fun <C : AbstractCommand> register(command: C): C {
        plugin.commandRegistry.registerCommand(command)
        return command
    }
}
