@file:JvmName("QueryExtensions")

package com.github.BetrixDev.hytaleKt.ecs.query

import com.hypixel.hytale.component.ComponentType
import com.hypixel.hytale.component.query.AndQuery
import com.hypixel.hytale.component.query.NotQuery
import com.hypixel.hytale.component.query.OrQuery
import com.hypixel.hytale.component.query.Query
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Creates an AND query from multiple component types.
 *
 * Example:
 * ```kotlin
 * val query = queryAnd(transformType, velocityType, playerType)
 * ```
 *
 * @param componentTypes The component types to combine with AND
 * @return An AND query matching entities with all specified components
 */
public fun queryAnd(
    vararg componentTypes: ComponentType<EntityStore, *>
): AndQuery<EntityStore> = Query.and(*componentTypes)

/**
 * Creates an OR query from multiple component types.
 *
 * Example:
 * ```kotlin
 * val query = queryOr(npcType, playerType)
 * ```
 *
 * @param componentTypes The component types to combine with OR
 * @return An OR query matching entities with any of the specified components
 */
public fun queryOr(
    vararg componentTypes: ComponentType<EntityStore, *>
): OrQuery<EntityStore> = Query.or(*componentTypes)

/**
 * Creates a NOT query that excludes entities with the specified component.
 *
 * Example:
 * ```kotlin
 * val query = queryNot(intangibleType)
 * ```
 *
 * @param componentType The component type to exclude
 * @return A NOT query excluding entities with the specified component
 */
public fun queryNot(
    componentType: ComponentType<EntityStore, *>
): NotQuery<EntityStore> = Query.not(componentType)

/**
 * Creates a NOT query that excludes entities matching the specified query.
 *
 * Example:
 * ```kotlin
 * val query = queryNot(queryOr(intangibleType, invisibleType))
 * ```
 *
 * @param query The query to negate
 * @return A NOT query excluding entities matching the specified query
 */
public fun queryNot(
    query: Query<EntityStore>
): NotQuery<EntityStore> = Query.not(query)

/**
 * Combines this query with another using AND.
 *
 * Example:
 * ```kotlin
 * val query = transformType and velocityType
 * ```
 *
 * @param other The other query to combine with
 * @return An AND query combining both queries
 */
public infix fun Query<EntityStore>.and(
    other: Query<EntityStore>
): AndQuery<EntityStore> = Query.and(this, other)

/**
 * Combines this component type with another using AND.
 *
 * Example:
 * ```kotlin
 * val query = transformType and velocityType and playerType
 * ```
 *
 * @param other The other component type to combine with
 * @return An AND query combining both component types
 */
public infix fun ComponentType<EntityStore, *>.and(
    other: ComponentType<EntityStore, *>
): AndQuery<EntityStore> = Query.and(this, other)

/**
 * Combines this component type with a query using AND.
 *
 * Example:
 * ```kotlin
 * val query = transformType and queryNot(intangibleType)
 * ```
 *
 * @param query The query to combine with
 * @return An AND query combining the component type and query
 */
public infix fun ComponentType<EntityStore, *>.and(
    query: Query<EntityStore>
): AndQuery<EntityStore> = Query.and(this, query)

/**
 * Combines this query with a component type using AND.
 *
 * @param componentType The component type to combine with
 * @return An AND query combining both
 */
public infix fun Query<EntityStore>.and(
    componentType: ComponentType<EntityStore, *>
): AndQuery<EntityStore> = Query.and(this, componentType)

/**
 * Combines this query with another using OR.
 *
 * Example:
 * ```kotlin
 * val query = npcQuery or playerQuery
 * ```
 *
 * @param other The other query to combine with
 * @return An OR query combining both queries
 */
public infix fun Query<EntityStore>.or(
    other: Query<EntityStore>
): OrQuery<EntityStore> = Query.or(this, other)

/**
 * Combines this component type with another using OR.
 *
 * Example:
 * ```kotlin
 * val query = npcType or playerType
 * ```
 *
 * @param other The other component type to combine with
 * @return An OR query combining both component types
 */
public infix fun ComponentType<EntityStore, *>.or(
    other: ComponentType<EntityStore, *>
): OrQuery<EntityStore> = Query.or(this, other)

/**
 * Creates a NOT query from this component type.
 *
 * Example:
 * ```kotlin
 * val query = transformType and !intangibleType
 * ```
 *
 * @return A NOT query excluding entities with this component
 */
public operator fun ComponentType<EntityStore, *>.not(): NotQuery<EntityStore> =
    Query.not(this)

/**
 * Creates a NOT query from this query.
 *
 * Example:
 * ```kotlin
 * val query = transformType and !queryOr(intangibleType, invisibleType)
 * ```
 *
 * @return A NOT query excluding entities matching this query
 */
public operator fun Query<EntityStore>.not(): NotQuery<EntityStore> =
    Query.not(this)
