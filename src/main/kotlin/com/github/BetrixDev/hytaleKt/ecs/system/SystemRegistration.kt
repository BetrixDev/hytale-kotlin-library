@file:JvmName("SystemRegistration")

package com.github.BetrixDev.hytaleKt.ecs.system

import com.hypixel.hytale.component.system.System
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Registers a system with the entity store registry.
 *
 * @param S The system type
 * @param system The system instance to register
 * @return The registered system (for chaining)
 */
public fun <S : System<EntityStore>> JavaPlugin.registerSystem(system: S): S {
    entityStoreRegistry.registerSystem(system)
    return system
}

/**
 * Registers multiple systems with the entity store registry.
 *
 * Example:
 * ```kotlin
 * registerSystems(
 *     RegenerationSystem(regenType, entityStatMapType),
 *     SkeletonsBowSystem(skeletonsBowArrowType, ropedArrowType),
 *     QueueMatchmakingSystem(queuedPlayerType, queueService, matchService)
 * )
 * ```
 *
 * @param systems The systems to register
 */
public fun JavaPlugin.registerSystems(vararg systems: System<EntityStore>) {
    systems.forEach { entityStoreRegistry.registerSystem(it) }
}

/**
 * Registers systems using a configuration block.
 *
 * Example:
 * ```kotlin
 * registerSystems {
 *     +RegenerationSystem(regenType, entityStatMapType)
 *     +SkeletonsBowSystem(skeletonsBowArrowType, ropedArrowType)
 *     +QueueMatchmakingSystem(queuedPlayerType, queueService, matchService)
 * }
 * ```
 *
 * @param block The configuration block
 */
public inline fun JavaPlugin.registerSystems(block: SystemRegistrationScope.() -> Unit) {
    SystemRegistrationScope(this).apply(block)
}

/**
 * Scope for registering multiple systems with a DSL.
 */
public class SystemRegistrationScope(
    @PublishedApi internal val plugin: JavaPlugin
) {
    /**
     * Registers a system using the unary plus operator.
     *
     * @param system The system to register
     * @return The registered system
     */
    public operator fun <S : System<EntityStore>> S.unaryPlus(): S {
        plugin.entityStoreRegistry.registerSystem(this)
        return this
    }

    /**
     * Registers a system.
     *
     * @param system The system to register
     * @return The registered system
     */
    public fun <S : System<EntityStore>> register(system: S): S {
        plugin.entityStoreRegistry.registerSystem(system)
        return system
    }
}
