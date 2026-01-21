@file:JvmName("PlayerCommands")

package dev.betrix.hytale.kotlin.commands

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.command.system.CommandContext
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * DSL marker for command builder to prevent nested DSL scope leaking.
 */
@DslMarker
public annotation class CommandDslMarker

/**
 * Builder for creating player commands using a DSL.
 *
 * Example:
 * ```kotlin
 * val command = playerCommand("queue", "Join the matchmaking queue") {
 *     aliases("q")
 *     permission = "ssm.commands.queue"
 *
 *     execute { player, context ->
 *         context.msg("Joining queue...")
 *     }
 * }
 * ```
 *
 * @param name The command name
 * @param description The command description
 */
@CommandDslMarker
public class PlayerCommandBuilder(
    private val name: String,
    private val description: String
) {
    private val aliases = mutableListOf<String>()
    private var permissionValue: String? = null
    private var executor: ((PlayerRef, CommandContext, Store<EntityStore>, Ref<EntityStore>, World) -> Unit)? = null

    /**
     * The permission required to execute this command.
     */
    public var permission: String?
        get() = permissionValue
        set(value) {
            permissionValue = value
        }

    /**
     * Adds aliases for this command.
     *
     * @param names The alias names
     */
    public fun aliases(vararg names: String) {
        aliases.addAll(names)
    }

    /**
     * Sets the command executor with minimal parameters.
     * Use this when you only need access to the player reference and command context.
     *
     * @param block The executor block
     */
    public fun execute(block: (player: PlayerRef, context: CommandContext) -> Unit) {
        executor = { playerRef, context, _, _, _ -> block(playerRef, context) }
    }

    /**
     * Sets the command executor with store access.
     * Use this when you need access to the entity store and reference.
     *
     * @param block The executor block
     */
    public fun executeWithStore(
        block: (player: PlayerRef, context: CommandContext, store: Store<EntityStore>, ref: Ref<EntityStore>) -> Unit
    ) {
        executor = { playerRef, context, store, ref, _ -> block(playerRef, context, store, ref) }
    }

    /**
     * Sets the command executor with full access including world.
     * Use this when you need access to all parameters including the world.
     *
     * @param block The executor block
     */
    public fun executeWithWorld(
        block: (player: PlayerRef, context: CommandContext, store: Store<EntityStore>, ref: Ref<EntityStore>, world: World) -> Unit
    ) {
        executor = block
    }

    /**
     * Builds the command instance.
     *
     * @return The built command
     */
    public fun build(): AbstractPlayerCommand = object : AbstractPlayerCommand(name, description) {
        init {
            if (aliases.isNotEmpty()) {
                addAliases(*aliases.toTypedArray())
            }
        }

        override fun execute(
            context: CommandContext,
            store: Store<EntityStore>,
            ref: Ref<EntityStore>,
            playerRef: PlayerRef,
            world: World
        ) {
            executor?.invoke(playerRef, context, store, ref, world)
        }
    }
}

/**
 * Creates a player command using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val hubCommand = playerCommand("hub", "Teleport to the HUB") {
 *     aliases("lobby", "spawn")
 *
 *     execute { player, context ->
 *         hubService.teleportToHub(player)
 *         context.msg("Teleporting to HUB...")
 *     }
 * }
 * ```
 *
 * @param name The command name
 * @param description The command description
 * @param block The builder configuration block
 * @return The built command
 */
public fun playerCommand(
    name: String,
    description: String,
    block: PlayerCommandBuilder.() -> Unit
): AbstractPlayerCommand = PlayerCommandBuilder(name, description).apply(block).build()
