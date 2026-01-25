---
title: Coroutines
description: Kotlin coroutine integration for async and world-thread operations
---

# Coroutines

Hytale.kt provides first-class coroutine support, making it easy to write async code that integrates seamlessly with Hytale's threading model.

## Overview

Hytale servers have specific threading requirements:

- **World Thread**: Each world runs on its own thread. Entity and block modifications must happen here.
- **Background Threads**: For I/O, computation, and other non-world operations.
- **Scheduled Executor**: Hytale's built-in single-threaded scheduler for timed tasks.

This library bridges Kotlin coroutines with these execution contexts.

## Quick Start

```kotlin
import dev.betrix.hytale.kotlin.coroutines.*
import kotlin.time.Duration.Companion.seconds

class MyPlugin : JavaPlugin() {

    override fun onLoad() {
        // Launch a coroutine tied to plugin lifecycle
        launch {
            val data = loadPlayerData() // Runs on default dispatcher
            world.withWorld {
                applyData(player, data) // Switches to world thread
            }
        }
    }

    override fun onDisable() {
        cancelPluginScope() // Cancel all plugin coroutines
    }
}
```

## Dispatchers

### HytaleDispatchers

The `HytaleDispatchers` object provides access to different execution contexts:

```kotlin
import dev.betrix.hytale.kotlin.coroutines.HytaleDispatchers

// CPU-bound background work
launch(HytaleDispatchers.async) {
    val result = expensiveComputation()
}

// I/O operations (database, files, network)
launch(HytaleDispatchers.io) {
    val config = loadConfigFromDisk()
    val playerData = queryDatabase(playerId)
}

// Hytale's scheduled executor (rarely needed directly)
launch(HytaleDispatchers.scheduled) {
    // Runs on Hytale's global scheduled executor
}
```

### World Dispatcher

Every `World` has a `dispatcher` property for executing code on its thread:

```kotlin
// Get dispatcher for a world
val worldDispatcher = world.dispatcher

// Use with withContext
launch(HytaleDispatchers.io) {
    val data = loadFromDatabase()
    withContext(worldDispatcher) {
        // Now on world thread - safe to modify entities
        player.applyData(data)
    }
}
```

## World Coroutines

### world.launch

Launch a coroutine that runs entirely on the world thread:

```kotlin
world.launch {
    // Everything here runs on the world thread
    player.teleport(spawnPoint)
    delay(1.seconds) // Suspends without blocking
    player.sendMessage("Welcome!")
}
```

### world.withWorld

Switch to the world thread from any coroutine context:

```kotlin
launchIO {
    val savedData = loadPlayerData(playerId)

    world.withWorld {
        // Switched to world thread
        player.inventory.apply(savedData.inventory)
        player.teleport(savedData.position)
    }

    // Back on IO dispatcher
    logPlayerLoad(playerId)
}
```

### world.async

Launch an async coroutine on the world thread that returns a value:

```kotlin
val playerCount: Deferred<Int> = world.async {
    countPlayersInRegion(region)
}

// Await the result from another context
val count = playerCount.await()
```

### world.runOnWorld

Execute a simple block on the world thread and await completion:

```kotlin
val position = world.runOnWorld {
    player.position.copy()
}
```

## Plugin Coroutines

### Plugin Scope

Every plugin has a `pluginScope` that automatically manages coroutine lifecycle:

```kotlin
class MyPlugin : JavaPlugin() {

    override fun onLoad() {
        // Coroutines launched here are tied to plugin lifecycle
        pluginScope.launch {
            while (isActive) {
                performMaintenance()
                delay(5.minutes)
            }
        }
    }

    override fun onDisable() {
        // Cancel all coroutines when plugin is disabled
        cancelPluginScope()
    }
}
```

### Convenience Functions

The library provides shorthand functions on `JavaPlugin`:

```kotlin
// Launch on default dispatcher
launch {
    doSomething()
}

// Launch on async dispatcher (CPU-bound work)
launchAsync {
    val result = computeExpensiveValue()
    world.withWorld { applyResult(result) }
}

// Launch on IO dispatcher (database, files)
launchIO {
    savePlayerData(player, data)
}

// Async with result
val config = async(HytaleDispatchers.io) {
    loadConfig()
}
```

## Tick-Based Delays

Hytale runs at 30 TPS (ticks per second). The library provides tick-aware delay utilities:

### Tick Duration Conversions

```kotlin
import dev.betrix.hytale.kotlin.coroutines.ticks

delay(1.ticks)   // ~33ms (one tick)
delay(30.ticks)  // ~1 second
delay(60.ticks)  // ~2 seconds
```

### delayTicks

```kotlin
world.launch {
    player.sendMessage("Get ready...")
    delayTicks(30) // Wait 1 second (30 ticks)
    player.sendMessage("Go!")
}
```

### repeatTicks

Execute an action for a specific number of ticks:

```kotlin
world.launch {
    // 3 second countdown
    repeatTicks(90) { tick ->
        if (tick % 30 == 0) {
            val secondsLeft = 3 - (tick / 30)
            player.sendMessage("$secondsLeft...")
        }
    }
    player.sendMessage("Go!")
}
```

### repeatEvery

Execute an action repeatedly until cancelled:

```kotlin
val updateJob = world.launch {
    repeatEvery(5.ticks) {
        updateScoreboard()
    }
}

// Later, stop the updates
updateJob.cancel()
```

Or using ticks directly:

```kotlin
world.launch {
    repeatEveryTicks(30) { // Every second
        broadcastPlayerCount()
    }
}
```

## Common Patterns

### Load Data, Apply on World Thread

```kotlin
launchIO {
    val playerData = database.loadPlayer(playerId)
    val inventory = database.loadInventory(playerId)

    world.withWorld {
        player.apply {
            applyStats(playerData)
            applyInventory(inventory)
            teleport(playerData.lastPosition)
        }
    }
}
```

### Periodic Background Task

```kotlin
override fun onLoad() {
    launch {
        while (isActive) {
            launchIO {
                saveAllPlayerData()
            }.join() // Wait for save to complete

            delay(5.minutes)
        }
    }
}
```

### Timeout Pattern

```kotlin
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

launchIO {
    try {
        withTimeout(10.seconds) {
            val response = fetchExternalApi()
            world.withWorld { applyResponse(response) }
        }
    } catch (e: TimeoutCancellationException) {
        logger.warning("API request timed out")
    }
}
```

### Parallel Operations

```kotlin
launchIO {
    // Run multiple database queries in parallel
    val playerData = async { database.loadPlayer(playerId) }
    val guildData = async { database.loadGuild(guildId) }
    val achievements = async { database.loadAchievements(playerId) }

    // Wait for all and apply on world thread
    world.withWorld {
        player.apply {
            applyPlayerData(playerData.await())
            applyGuildData(guildData.await())
            applyAchievements(achievements.await())
        }
    }
}
```

### Batched Processing

Process large collections without blocking the world thread:

```kotlin
suspend fun processEntitiesInBatches(entities: List<Entity>, batchSize: Int = 10) {
    entities.chunked(batchSize).forEach { batch ->
        world.withWorld {
            batch.forEach { entity ->
                processEntity(entity)
            }
        }
        delay(1.ticks) // Yield to let other work happen
    }
}
```

### Async Player Join with Ban Check

Check if a player is banned from an external API before allowing them to join, without blocking other server operations:

```kotlin
// Fake ban check API (replace with your actual API)
object BanApi {
    suspend fun isPlayerBanned(uuid: UUID): Boolean {
        // Simulate external API call
        delay(500.milliseconds)
        // Your actual ban check logic here
        return false
    }
}

// Handle player connection event
override fun onLoad() {
    // Register for PlayerConnectEvent
    registerEvent<PlayerConnectEvent> { event ->
        val playerRef = event.playerRef
        val playerUuid = playerRef.uuid

        // Launch coroutine to check ban status asynchronously
        // This doesn't block the event handler or any other threads
        launch {
            try {
                withTimeout(10.seconds) {
                    // Check ban status on IO dispatcher (network)
                    val isBanned = withContext(HytaleDispatchers.io) {
                        BanApi.isPlayerBanned(playerUuid)
                    }

                    if (isBanned) {
                        // Player is banned - kick them
                        // Note: You'll need to implement the kick logic
                        // based on Hytale's actual API
                        kickPlayer(playerRef, "You are banned from this server")
                        logger.info("Blocked banned player: ${playerRef.username}")
                    } else {
                        // Player is not banned - allow entry
                        // Load their data asynchronously
                        val playerData = withContext(HytaleDispatchers.io) {
                            loadPlayerData(playerUuid)
                        }
                        
                        world.withWorld {
                            // Apply player data once they enter the world
                            applyPlayerData(playerRef, playerData)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                logger.warning("Ban check timed out for ${playerRef.username}, allowing join")
            } catch (e: Exception) {
                logger.warning("Error checking ban status: ${e.message}")
            }
        }
    }
}

suspend fun kickPlayer(playerRef: PlayerRef, reason: String) {
    // TODO: Implement actual kick logic using Hytale's API
    // The exact API depends on Hytale's implementation
    // This would typically involve disconnecting the player session
    
    // Placeholder for the actual implementation
    logger.info("Kicking ${playerRef.username}: $reason")
}
```

**Why this doesn't block:**

- The event handler returns immediately after launching the coroutine
- The ban check runs on `HytaleDispatchers.io` (separate thread from world/main thread)
- Other players can join and other server operations continue normally
- Only when the async check completes is the player kicked or allowed through

**The flow:**

1. Player connects → `PlayerConnectEvent` fires
2. Event handler launches coroutine and returns immediately
3. Server continues processing other players/events
4. Coroutine checks ban status in background (non-blocking)
5. After check completes, player is either kicked or allowed to proceed

This pattern ensures that slow external API calls don't slow down the server's overall performance.

## Best Practices

### 1. Always Cancel Plugin Scope

```kotlin
override fun onDisable() {
    cancelPluginScope()
}
```

### 2. Use the Right Dispatcher

| Task Type                 | Dispatcher                |
| ------------------------- | ------------------------- |
| Entity/block modification | `world.dispatcher`        |
| Database queries          | `HytaleDispatchers.io`    |
| File I/O                  | `HytaleDispatchers.io`    |
| CPU computation           | `HytaleDispatchers.async` |
| Network requests          | `HytaleDispatchers.io`    |

### 3. Never Block the World Thread

```kotlin
// BAD - blocks the world thread
world.launch {
    Thread.sleep(1000) // Never do this!
    val data = database.query() // Blocking I/O on world thread!
}

// GOOD - suspends properly
world.launch {
    delay(1.seconds) // Suspends, doesn't block
}

// GOOD - I/O on proper dispatcher
launchIO {
    val data = database.query()
    world.withWorld { apply(data) }
}
```

### 4. Handle Exceptions

```kotlin
launch {
    try {
        riskyOperation()
    } catch (e: Exception) {
        logger.severe("Operation failed: ${e.message}")
    }
}

// Or use supervisorScope for partial failure handling
launch {
    supervisorScope {
        launch { task1() } // Failure here won't cancel task2
        launch { task2() }
    }
}
```

### 5. Use Structured Concurrency

Keep coroutine hierarchies clean so cancellation propagates correctly:

```kotlin
world.launch {
    // Child coroutines are automatically cancelled if parent is cancelled
    val job1 = launch { subtask1() }
    val job2 = launch { subtask2() }

    // Wait for both
    job1.join()
    job2.join()
}
```
