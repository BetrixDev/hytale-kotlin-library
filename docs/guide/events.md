# Events

The events module provides Kotlin extensions for registering event handlers with type inference, eliminating the need to pass class references explicitly.

## Overview

Hytale's event system allows plugins to react to game events like player connections, world loading, and entity interactions. The library simplifies registration with reified generics.

## Event Types

Hytale has two categories of events:

### Keyed Events
Events associated with a specific entity or object (e.g., player events).

```kotlin
registerEvent<PlayerConnectEvent> { event ->
    logger.info("Player connected: ${event.playerName}")
}
```

### Global Events
Events not tied to a specific entity (e.g., world lifecycle events).

```kotlin
registerGlobalEvent<AllWorldsLoadedEvent> { event ->
    initializePlugin()
}
```

## Basic Registration

### Single Event

```kotlin
// Keyed event (IBaseEvent<Void>)
registerEvent<PlayerConnectEvent> { event ->
    sendWelcomeMessage(event)
}

// Global event
registerGlobalEvent<WorldLoadEvent> { event ->
    logger.info("World loaded: ${event.world.name}")
}
```

### Multiple Events

The DSL block provides a cleaner way to register multiple handlers:

```kotlin
registerEvents {
    // Global events
    global<AllWorldsLoadedEvent> { initializeServices() }
    global<WorldUnloadEvent> { cleanupWorld(it.world) }
    
    // Keyed events
    on<PlayerConnectEvent> { onPlayerConnect(it) }
    on<PlayerDisconnectEvent> { onPlayerDisconnect(it) }
    on<EntityDeathEvent> { onEntityDeath(it) }
}
```

## Comparison with Raw Hytale API

```kotlin
// Hytale API - requires passing Class reference
eventRegistry.register(PlayerConnectEvent::class.java) { event ->
    // handle
}

// Kotlin Library - type inferred from generic
registerEvent<PlayerConnectEvent> { event ->
    // handle
}

// Hytale API - global events are more verbose
val eventClass = AllWorldsLoadedEvent::class.java as Class<IBaseEvent<Any>>
eventRegistry.registerGlobal(eventClass) { event ->
    // handle
}

// Kotlin Library - clean reified generics
registerGlobalEvent<AllWorldsLoadedEvent> {
    // handle
}
```

## Event Registration Returns

Registering keyed events returns an `EventRegistration` that can be used to unregister:

```kotlin
val registration = registerEvent<PlayerConnectEvent> { event ->
    // handle
}

// Later, to unregister:
registration.unregister()
```

::: warning
Global event registration does not return a registration object in the current implementation. If you need to unregister global events, you'll need to use the raw Hytale API.
:::

## Practical Examples

### Player Lifecycle Events

```kotlin
class PlayerManager(private val plugin: JavaPlugin) {
    
    private val activePlayers = mutableMapOf<UUID, PlayerData>()
    
    fun registerEvents() = with(plugin) {
        registerEvents {
            on<PlayerConnectEvent> { event ->
                val data = loadPlayerData(event.playerId)
                activePlayers[event.playerId] = data
                sendWelcomeMessage(event)
            }
            
            on<PlayerDisconnectEvent> { event ->
                activePlayers.remove(event.playerId)?.let { data ->
                    savePlayerData(data)
                }
            }
        }
    }
}
```

### World Initialization

```kotlin
override fun onLoad() {
    registerEvents {
        global<AllWorldsLoadedEvent> {
            // All worlds are now available
            val hubWorld = worldManager.getWorld("hub")
            hubService.initialize(hubWorld)
            
            val arenaWorld = worldManager.getWorld("arena")
            arenaService.initialize(arenaWorld)
            
            logger.info("All services initialized!")
        }
    }
}
```

### Combat Events

```kotlin
registerEvents {
    on<EntityDamageEvent> { event ->
        if (event.target.hasTag("invincible")) {
            event.cancel()
            return@on
        }
        
        if (event.damage > 50) {
            announceHighDamage(event)
        }
    }
    
    on<EntityDeathEvent> { event ->
        val killer = event.killer ?: return@on
        awardKillPoints(killer)
        trackKillStatistics(killer, event.entity)
    }
}
```

## Pros and Cons

### Pros

| Feature | Benefit |
|---------|---------|
| Reified generics | No explicit Class references |
| DSL block | Register multiple events cleanly |
| Type inference | IDE autocomplete for event properties |
| Inline functions | Zero runtime overhead |

### Cons

| Limitation | Context |
|------------|---------|
| No return value for global events | Can't easily unregister |
| Requires Kotlin | Java callers must use raw Hytale API |
| Suppressed unchecked casts | Internal type casts for reification |

## Event Handling Best Practices

### 1. Keep Handlers Lightweight

Event handlers should be fast. Defer heavy work:

```kotlin
on<PlayerConnectEvent> { event ->
    // Bad: Heavy operation in handler
    // loadAndProcessAllPlayerData(event.playerId)
    
    // Good: Schedule for later
    scheduler.runAsync {
        loadAndProcessAllPlayerData(event.playerId)
    }
}
```

### 2. Handle Exceptions

Wrap handlers to prevent one failure from breaking others:

```kotlin
inline fun <reified E : IBaseEvent<Void>> JavaPlugin.safeRegisterEvent(
    crossinline handler: (E) -> Unit
) = registerEvent<E> { event ->
    try {
        handler(event)
    } catch (e: Exception) {
        logger.error("Error handling ${E::class.simpleName}", e)
    }
}
```

### 3. Use Meaningful Function Names

Extract handler logic to named functions for clarity:

```kotlin
registerEvents {
    on<PlayerConnectEvent> { handlePlayerConnect(it) }
    on<PlayerDisconnectEvent> { handlePlayerDisconnect(it) }
}

private fun handlePlayerConnect(event: PlayerConnectEvent) {
    // Clear, testable logic here
}
```

### 4. Group Related Events

Use the DSL block to group related event handlers:

```kotlin
// Combat events
registerEvents {
    on<EntityDamageEvent> { /* ... */ }
    on<EntityDeathEvent> { /* ... */ }
    on<EntityRespawnEvent> { /* ... */ }
}

// UI events
registerEvents {
    on<PlayerOpenMenuEvent> { /* ... */ }
    on<PlayerCloseMenuEvent> { /* ... */ }
}
```

## Event Priority

::: info
The current library version doesn't provide priority extensions. Use the raw Hytale API for priority-based event handling:

```kotlin
eventRegistry.register(SomeEvent::class.java, EventPriority.HIGH) { event ->
    // Handle with high priority
}
```
:::

## Common Events Reference

| Event | Type | Description |
|-------|------|-------------|
| `AllWorldsLoadedEvent` | Global | Fired when all worlds finish loading |
| `WorldLoadEvent` | Global | Fired when a specific world loads |
| `PlayerConnectEvent` | Keyed | Player joins the server |
| `PlayerDisconnectEvent` | Keyed | Player leaves the server |
| `EntityDamageEvent` | Keyed | Entity takes damage |
| `EntityDeathEvent` | Keyed | Entity dies |
| `BlockBreakEvent` | Keyed | Block is broken |
| `BlockPlaceEvent` | Keyed | Block is placed |

For a complete list, refer to the decompiled Hytale server code in `com.hypixel.hytale.server.core.event`.
