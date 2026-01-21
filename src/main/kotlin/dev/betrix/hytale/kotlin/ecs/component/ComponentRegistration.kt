@file:JvmName("ComponentRegistration")

package dev.betrix.hytale.kotlin.ecs.component

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Registers a component type with the entity store registry.
 * Uses reified generics to infer the component class.
 *
 * Example:
 * ```kotlin
 * val activeKitType = registerComponent<ActiveKitComponent>("ActiveKit", ActiveKitComponent.CODEC)
 * ```
 *
 * @param C The component type
 * @param name The unique name for this component type
 * @param codec The codec for serialization/deserialization
 * @return The registered component type
 */
public inline fun <reified C : Component<EntityStore>> JavaPlugin.registerComponent(
    name: String,
    codec: BuilderCodec<C>
): ComponentType<EntityStore, C> {
    return entityStoreRegistry.registerComponent(C::class.java, name, codec)
}

/**
 * Registers multiple components at once using a configuration block.
 *
 * Example:
 * ```kotlin
 * registerComponents {
 *     register<ActiveKitComponent>("ActiveKit", ActiveKitComponent.CODEC)
 *     register<RegenerationComponent>("Regeneration", RegenerationComponent.CODEC)
 * }
 * ```
 *
 * @param block The configuration block
 */
public inline fun JavaPlugin.registerComponents(block: ComponentRegistrationScope.() -> Unit) {
    ComponentRegistrationScope(this).apply(block)
}

/**
 * Scope for registering multiple components.
 */
public class ComponentRegistrationScope(
    @PublishedApi internal val plugin: JavaPlugin
) {
    /**
     * Registers a component type within this scope.
     *
     * @param C The component type
     * @param name The unique name for this component type
     * @param codec The codec for serialization/deserialization
     * @return The registered component type
     */
    public inline fun <reified C : Component<EntityStore>> register(
        name: String,
        codec: BuilderCodec<C>
    ): ComponentType<EntityStore, C> {
        return plugin.entityStoreRegistry.registerComponent(C::class.java, name, codec)
    }
}
