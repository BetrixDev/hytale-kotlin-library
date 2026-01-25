@file:JvmName("ComponentExtensions")

package com.github.BetrixDev.hytaleKt.ecs.component

import com.hypixel.hytale.component.ArchetypeChunk
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Gets a component from the store, or returns null if not present.
 * This is functionally equivalent to [Store.getComponent] but makes
 * nullability explicit in the method name for Kotlin code clarity.
 *
 * @param ref The entity reference
 * @param type The component type to retrieve
 * @return The component instance or null
 */
public fun <C : Component<EntityStore>> Store<EntityStore>.getComponentOrNull(
    ref: Ref<EntityStore>,
    type: ComponentType<EntityStore, C>
): C? = getComponent(ref, type)

/**
 * Gets a required component from the store, throwing if not present.
 *
 * @param ref The entity reference
 * @param type The component type to retrieve
 * @return The component instance
 * @throws IllegalStateException if the component is not found
 */
public fun <C : Component<EntityStore>> Store<EntityStore>.requireComponent(
    ref: Ref<EntityStore>,
    type: ComponentType<EntityStore, C>
): C = getComponent(ref, type)
    ?: throw IllegalStateException("Required component not found: ${type.typeClass.simpleName}")

/**
 * Gets a component from an archetype chunk, or returns null if not present.
 *
 * @param index The entity index within the chunk
 * @param type The component type to retrieve
 * @return The component instance or null
 */
public fun <C : Component<EntityStore>> ArchetypeChunk<EntityStore>.getComponentOrNull(
    index: Int,
    type: ComponentType<EntityStore, C>
): C? = getComponent(index, type)

/**
 * Gets a required component from an archetype chunk, throwing if not present.
 *
 * @param index The entity index within the chunk
 * @param type The component type to retrieve
 * @return The component instance
 * @throws IllegalStateException if the component is not found
 */
public fun <C : Component<EntityStore>> ArchetypeChunk<EntityStore>.requireComponent(
    index: Int,
    type: ComponentType<EntityStore, C>
): C = getComponent(index, type)
    ?: throw IllegalStateException("Required component not found: ${type.typeClass.simpleName}")

/**
 * Executes a block with a component if it exists.
 *
 * @param ref The entity reference
 * @param type The component type to retrieve
 * @param block The block to execute with the component
 * @return The result of the block, or null if the component wasn't found
 */
public inline fun <C : Component<EntityStore>, R> Store<EntityStore>.withComponent(
    ref: Ref<EntityStore>,
    type: ComponentType<EntityStore, C>,
    block: (C) -> R
): R? = getComponent(ref, type)?.let(block)

/**
 * Checks if an entity has a specific component.
 *
 * @param ref The entity reference
 * @param type The component type to check
 * @return true if the entity has the component
 */
public fun <C : Component<EntityStore>> Store<EntityStore>.hasComponent(
    ref: Ref<EntityStore>,
    type: ComponentType<EntityStore, C>
): Boolean = getComponent(ref, type) != null
