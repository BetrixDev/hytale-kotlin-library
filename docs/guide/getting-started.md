# Getting Started

This guide will walk you through setting up and using the Hytale Kotlin Library in your server plugin project.

## Prerequisites

- **Java 21** - Required by Hytale's server
- **Kotlin 2.0+** - The library uses modern Kotlin features
- **Hytale Server** - Installed locally for development

## Installation

### Gradle (Kotlin DSL)

Add the library to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.betrix.hytale.kotlin:hytale-kotlin-library:0.1.0")
    compileOnly(files("path/to/HytaleServer.jar"))
}
```

The library automatically locates your Hytale installation, but you can override it:

```kotlin
// In gradle.properties
hytale_home=/custom/path/to/Hytale
patchline=release
```

### Compiler Options

The library is compiled with strict explicit API mode. For best compatibility, configure your project similarly:

```kotlin
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all"  // Generate default methods in interfaces
        )
    }
}
```

## Project Structure

A typical plugin using this library might look like:

```
my-plugin/
├── src/main/kotlin/
│   └── com/example/myplugin/
│       ├── MyPlugin.kt           # Main plugin class
│       ├── commands/             # Command implementations
│       ├── components/           # ECS components
│       ├── systems/              # ECS systems
│       ├── events/               # Event handlers
│       └── ui/                   # HUD and page implementations
└── build.gradle.kts
```

## Your First Plugin

Here's a minimal plugin using the library:

```kotlin
package com.example.myplugin

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import dev.betrix.hytale.kotlin.commands.*
import dev.betrix.hytale.kotlin.events.*
import dev.betrix.hytale.kotlin.core.msg

class MyPlugin : JavaPlugin() {
    
    override fun onLoad() {
        // Register a simple command
        registerCommand(
            playerCommand("hello", "Says hello to the player") {
                execute { player, context ->
                    context.msg("Hello, ${player.name}!")
                }
            }
        )
        
        logger.info("MyPlugin loaded!")
    }
}
```

## Understanding the Architecture

The Hytale server uses an **Entity-Component-System (ECS)** architecture. Understanding this is crucial for effective plugin development:

### Components
Data containers attached to entities. They hold state but no logic.

```kotlin
class MatchComponent : Component<EntityStore>() {
    var matchId: UUID = UUID.randomUUID()
    var state: MatchState = MatchState.WAITING
    var round: Int = 0
}
```

### Systems
Process entities with specific components each tick.

```kotlin
class MatchTickSystem(
    private val matchType: ComponentType<EntityStore, MatchComponent>
) : TypedEntitySystem(matchType) {
    
    override fun tick(dt: Float, index: Int, chunk: ArchetypeChunk<EntityStore>, 
                      store: Store<EntityStore>, buffer: CommandBuffer<EntityStore>) {
        val match = chunk.getComponent(index, matchType) ?: return
        // Update match state...
    }
}
```

### Events
Notifications about things happening in the game.

```kotlin
registerEvent<PlayerConnectEvent> { event ->
    // Player connected
}
```

## Next Steps

- **[Commands Guide](/guide/commands)** - Learn to build complex commands with arguments
- **[ECS Guide](/guide/ecs)** - Deep dive into components, systems, and queries
- **[Events Guide](/guide/events)** - Handle game events effectively
- **[UI Guide](/guide/ui)** - Build HUDs and interactive pages

## Design Philosophy

This library follows these principles:

1. **Wrap, don't replace** - Extends Hytale's API rather than hiding it
2. **Type safety first** - Leverage Kotlin's type system to catch errors at compile time
3. **Explicit nullability** - Clear APIs for handling missing data
4. **Minimal overhead** - Inline functions and extension functions add no runtime cost
5. **Familiar patterns** - Uses common Kotlin idioms like DSL builders and scope functions
