# Commands

The commands module provides Kotlin DSLs for building and registering player commands with significantly less boilerplate than the raw Hytale API.

## Overview

Hytale's command system requires extending abstract classes and overriding methods with multiple parameters. This library provides a DSL that makes command creation concise while maintaining full access to all command features.

## Creating a Simple Command

The `playerCommand` function creates commands executable by players:

```kotlin
val hubCommand = playerCommand("hub", "Teleport to the hub world") {
    aliases("lobby", "spawn", "l")
    
    execute { player, context ->
        hubService.teleportToHub(player)
        context.msg("Teleporting to hub...")
    }
}
```

### Pros
- Significantly less boilerplate than extending `AbstractPlayerCommand`
- Compile-time type safety
- Clear, readable DSL syntax

### Cons
- Cannot access protected methods of the abstract command class
- Slightly less flexible for very complex commands with custom lifecycle

## Command Execution Levels

The DSL provides three levels of access depending on what you need:

### Basic Execution

When you only need the player reference and context:

```kotlin
playerCommand("ping", "Check your connection") {
    execute { player, context ->
        context.msg("Pong! Your connection is stable.")
    }
}
```

### Store Access

When you need to read/write components on the player entity:

```kotlin
playerCommand("stats", "View your statistics") {
    executeWithStore { player, context, store, ref ->
        val stats = store.getComponent(ref, statsType) ?: run {
            context.msg("No stats found!")
            return@executeWithStore
        }
        context.msg("Kills: ${stats.kills}, Deaths: ${stats.deaths}")
    }
}
```

### Full World Access

When you need access to the world for spawning entities or world operations:

```kotlin
playerCommand("spawn-npc", "Spawn an NPC at your location") {
    executeWithWorld { player, context, store, ref, world ->
        val transform = store.getComponent(ref, transformType) ?: return@executeWithWorld
        world.spawnEntity(npcPrefab, transform.position)
        context.msg("NPC spawned!")
    }
}
```

## Command Registration

### Single Command

```kotlin
registerCommand(hubCommand)
```

### Multiple Commands

```kotlin
registerCommands(hubCommand, queueCommand, statsCommand)
```

### DSL Block

```kotlin
registerCommands {
    +hubCommand
    +queueCommand
    
    // Create and register inline
    +playerCommand("test", "A test command") {
        execute { _, context -> context.msg("Test!") }
    }
}
```

## Working with Arguments

Hytale commands use an argument system. This library provides extensions to work with them more naturally in Kotlin.

### Getting Optional Arguments

```kotlin
// Standard Hytale way (doesn't communicate nullability well)
val value = if (context.provided(myArg)) myArg.get(context) else null

// Kotlin extension
val value = myArg.getOrNull(context)
```

### Default Values

```kotlin
val count = countArg.getOrDefault(context, 1)
val radius = radiusArg.getOrDefault(context, 10.0)
```

### Conditional Execution

```kotlin
// Execute a block only if an argument was provided
radiusArg.ifProvided(context) { radius ->
    setSearchRadius(radius)
}

// Transform an argument value if provided
val player = playerNameArg.mapIfProvided(context) { name ->
    findPlayerByName(name)
}
```

### Required Arguments

For arguments that must always be present:

```kotlin
try {
    val target = targetArg.require(context)
    // target is guaranteed non-null here
} catch (e: IllegalStateException) {
    context.msg("Target is required!")
}
```

## Message Utilities

The library provides shorthand methods for sending messages:

```kotlin
// Simple message
context.msg("Hello, world!")

// Multiple lines
context.msgLines(
    "Welcome to the server!",
    "Type /help for commands",
    "Have fun!"
)

// Formatted message
context.msgf("Player %s has %d kills", playerName, killCount)
```

## Complete Example

Here's a real-world command with arguments and error handling:

```kotlin
class QueueCommand(
    private val queueService: QueueService
) : AbstractPlayerCommand("queue", "Join or leave the matchmaking queue") {
    
    private val actionArg = addArg(StringArgument("action", "join|leave|status"))
    private val gameModeArg = addArg(StringArgument("mode", "The game mode").optional())
    
    override fun execute(
        context: CommandContext,
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        playerRef: PlayerRef,
        world: World
    ) {
        val action = actionArg.getOrDefault(context, "status")
        
        when (action) {
            "join" -> {
                val mode = gameModeArg.getOrNull(context) ?: run {
                    context.msg("Usage: /queue join <mode>")
                    return
                }
                queueService.join(playerRef, mode)
                context.msg("Joined $mode queue!")
            }
            "leave" -> {
                queueService.leave(playerRef)
                context.msg("Left the queue.")
            }
            "status" -> {
                val status = queueService.getStatus(playerRef)
                context.msg("Queue status: $status")
            }
            else -> context.msg("Unknown action: $action")
        }
    }
}
```

## Pros and Cons Summary

### Pros

| Feature | Benefit |
|---------|---------|
| DSL syntax | Readable, less boilerplate |
| `getOrNull` | Explicit null handling |
| `ifProvided` | Clean conditional logic |
| `msg` extensions | Less verbose messaging |
| Type inference | No explicit generics needed |

### Cons

| Limitation | Workaround |
|------------|------------|
| Can't access protected base class methods | Extend the class directly for complex cases |
| DSL commands are anonymous classes | Use named classes for commands needing identity |
| No argument validation DSL yet | Use Hytale's built-in argument validation |

## When to Use DSL vs. Class Extension

**Use the DSL when:**
- Building simple commands quickly
- The command doesn't need custom lifecycle methods
- You want maximum readability

**Extend the class directly when:**
- You need access to protected methods
- The command has complex initialization
- You need to override multiple lifecycle methods
- You want to share argument definitions across commands
