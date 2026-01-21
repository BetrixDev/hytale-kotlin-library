package dev.betrix.hytale.kotlin.ecs.system

import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.component.system.tick.EntityTickingSystem
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Abstract base class for entity ticking systems that automatically builds
 * the query from the provided component types.
 *
 * This eliminates the boilerplate of manually constructing queries and
 * implementing [getQuery].
 *
 * Example:
 * ```kotlin
 * class RegenerationSystem(
 *     private val regenType: ComponentType<EntityStore, RegenerationComponent>,
 *     private val statMapType: ComponentType<EntityStore, EntityStatMap>
 * ) : TypedEntitySystem(regenType, statMapType) {
 *
 *     override fun tick(
 *         dt: Float,
 *         index: Int,
 *         chunk: ArchetypeChunk<EntityStore>,
 *         store: Store<EntityStore>,
 *         buffer: CommandBuffer<EntityStore>
 *     ) {
 *         val regen = chunk.getComponent(index, regenType) ?: return
 *         val statMap = chunk.getComponent(index, statMapType) ?: return
 *         // ... system logic
 *     }
 * }
 * ```
 *
 * @param componentTypes The component types this system requires. The query
 *                      will match entities that have ALL of these components.
 */
public abstract class TypedEntitySystem(
    private vararg val componentTypes: ComponentType<EntityStore, *>
) : EntityTickingSystem<EntityStore>() {

    init {
        require(componentTypes.isNotEmpty()) {
            "TypedEntitySystem requires at least one component type"
        }
    }

    /**
     * The lazily-constructed query that matches entities with all
     * specified component types.
     */
    private val _query: Query<EntityStore> by lazy {
        when (componentTypes.size) {
            1 -> componentTypes[0] as Query<EntityStore>
            2 -> Query.and(componentTypes[0], componentTypes[1])
            else -> {
                @Suppress("UNCHECKED_CAST")
                val queries = componentTypes as Array<out Query<EntityStore>>
                Query.and(*queries)
            }
        }
    }

    final override fun getQuery(): Query<EntityStore> = _query
}
