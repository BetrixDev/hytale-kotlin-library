@file:JvmName("EventRegistration")

package com.github.BetrixDev.hytaleKt.events

import com.hypixel.hytale.event.EventRegistration
import com.hypixel.hytale.event.IBaseEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin

/**
 * Registers an event handler with type inference from reified generics.
 * This is for keyed events (events bound to a specific entity/object).
 *
 * Example:
 * ```kotlin
 * registerEvent<PlayerConnectEvent> { event ->
 *     logger.info("Player connected: ${event.playerName}")
 * }
 * ```
 *
 * @param E The event type
 * @param handler The event handler
 * @return The event registration for potential unregistration
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified E : IBaseEvent<Void>> JavaPlugin.registerEvent(
    crossinline handler: (E) -> Unit
): EventRegistration<Void, E> =
    eventRegistry.register(E::class.java) { event ->
        handler(event as E)
    }!!

/**
 * Registers a global event handler with type inference from reified generics.
 * Global events are not keyed to a specific entity.
 *
 * Example:
 * ```kotlin
 * registerGlobalEvent<AllWorldsLoadedEvent> {
 *     hubService.initialize()
 * }
 * ```
 *
 * @param E The event type
 * @param handler The event handler
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified E : IBaseEvent<*>> JavaPlugin.registerGlobalEvent(
    crossinline handler: (E) -> Unit
) {
    val eventClass = E::class.java as Class<IBaseEvent<Any>>
    eventRegistry.registerGlobal(eventClass) { event ->
        handler(event as E)
    }
}

/**
 * Registers multiple events using a DSL block.
 *
 * Example:
 * ```kotlin
 * registerEvents {
 *     global<AllWorldsLoadedEvent> { hubService.initialize() }
 *     on<PlayerConnectEvent> { onPlayerConnect(it) }
 *     on<PlayerDisconnectEvent> { onPlayerDisconnect(it) }
 * }
 * ```
 *
 * @param block The configuration block
 */
public inline fun JavaPlugin.registerEvents(block: EventRegistrationScope.() -> Unit) {
    EventRegistrationScope(this).apply(block)
}

/**
 * Scope for registering multiple events with a DSL.
 */
public class EventRegistrationScope(
    @PublishedApi internal val plugin: JavaPlugin
) {
    /**
     * Registers a keyed event handler.
     *
     * Example:
     * ```kotlin
     * on<PlayerConnectEvent> { event ->
     *     sendWelcomeMessage(event)
     * }
     * ```
     *
     * @param E The event type
     * @param handler The event handler
     * @return The event registration
     */
    public inline fun <reified E : IBaseEvent<Void>> on(
        crossinline handler: (E) -> Unit
    ): EventRegistration<Void, E> = plugin.registerEvent(handler)

    /**
     * Registers a global event handler.
     *
     * Example:
     * ```kotlin
     * global<AllWorldsLoadedEvent> {
     *     initializeServices()
     * }
     * ```
     *
     * @param E The event type
     * @param handler The event handler
     */
    public inline fun <reified E : IBaseEvent<*>> global(
        crossinline handler: (E) -> Unit
    ) {
        plugin.registerGlobalEvent(handler)
    }
}
