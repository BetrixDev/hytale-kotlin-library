---
layout: home

hero:
  name: "Hytale.kt"
  text: ""
  tagline: Reduce boilerplate, increase type safety, and write cleaner server plugins, with speed
  actions:
    - theme: brand
      text: Getting Started
      link: /guide/getting-started
    - theme: alt
      text: API Reference
      link: /reference/overview

features:
  - title: Type-Safe Commands
    details: Build player commands with DSL builders that eliminate boilerplate and provide compile-time safety
  - title: ECS Made Simple
    details: Kotlin extensions for Hytale's Entity-Component-System architecture with intuitive query operators
  - title: Codec DSL
    details: Define component serialization with a type-safe builder that handles UUIDs, enums, and custom types
  - title: Event Registration
    details: Register event handlers with reified generics - no more passing class references
  - title: UI Extensions
    details: Cleaner HUD updates and page event binding with Kotlin-idiomatic APIs
  - title: Null Safety
    details: Explicit nullability throughout - getOrNull, require, and ifProvided patterns
---

## What is Hytale.kt?

This library provides Kotlin extensions and DSLs that wrap Hytale's Java server API, making plugin development more concise and type-safe. It doesn't replace the Hytale API - it enhances it with idiomatic Kotlin patterns.

```kotlin
// Before: Java-style Hytale API
val registry = eventRegistry
registry.register(PlayerConnectEvent::class.java) { event ->
    // handle event
}

// After: Kotlin DSL
registerEvent<PlayerConnectEvent> { event ->
    // handle event
}
```

## Quick Example

Here's a complete example showing how the library simplifies common tasks:

```kotlin
class MyPlugin : JavaPlugin() {

    private lateinit var matchType: ComponentType<EntityStore, MatchComponent>

    override fun onLoad() {
        // Register components with reified generics
        matchType = registerComponent("Match", MatchComponent.CODEC)

        // Register systems with DSL
        registerSystems {
            +MatchTickSystem(matchType)
            +ScoreboardUpdateSystem(matchType)
        }

        // Register commands with DSL
        registerCommands {
            +playerCommand("queue", "Join the matchmaking queue") {
                aliases("q")
                execute { player, context ->
                    context.msg("Joining queue...")
                }
            }
        }

        // Register events with reified generics
        registerEvents {
            global<AllWorldsLoadedEvent> { initializeServices() }
            on<PlayerConnectEvent> { handlePlayerConnect(it) }
        }
    }
}
```
