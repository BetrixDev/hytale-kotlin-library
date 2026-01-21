# API Reference

Quick reference for all extensions and utilities provided by the Hytale Kotlin Library.

## Commands

**Package:** `dev.betrix.hytale.kotlin.commands`

### JavaPlugin Extensions

| Extension | Description |
|-----------|-------------|
| `registerCommand(name, builder)` | Register a player command with DSL builder |

### PlayerCommandBuilder

| Method | Description |
|--------|-------------|
| `permission(permission)` | Set required permission |
| `description(text)` | Set command description |
| `onComplete(block)` | Tab completion handler |
| `onExecute(block)` | Command execution handler |

### CommandContext Extensions

| Extension | Signature | Description |
|-----------|-----------|-------------|
| `getOrNull` | `<T> CommandContext.getOrNull(name): T?` | Get argument or null |
| `getOrDefault` | `<T> CommandContext.getOrDefault(name, default): T` | Get argument with default |
| `ifProvided` | `<T> CommandContext.ifProvided(name, block)` | Execute if argument exists |
| `require` | `<T> CommandContext.require(name): T` | Get argument or throw |

---

## ECS Components

**Package:** `dev.betrix.hytale.kotlin.ecs.component`

### JavaPlugin Extensions

| Extension | Description |
|-----------|-------------|
| `registerComponent<T>(configure)` | Register component with reified type |
| `buildComponentCodec(type, builder)` | Build codec with DSL |

### Entity Extensions

| Extension | Signature | Description |
|-----------|-----------|-------------|
| `getComponentOrNull` | `<T> Entity.getComponentOrNull(): T?` | Get component or null |
| `requireComponent` | `<T> Entity.requireComponent(): T` | Get component or throw |
| `hasComponent` | `<T> Entity.hasComponent(): Boolean` | Check if has component |
| `withComponent` | `<T> Entity.withComponent(block)` | Execute with component if present |

### ComponentCodecBuilder

| Method | Description |
|--------|-------------|
| `field(name, getter, setter, codec)` | Add a codec field |
| `intField(name, getter, setter)` | Add int field |
| `floatField(name, getter, setter)` | Add float field |
| `doubleField(name, getter, setter)` | Add double field |
| `stringField(name, getter, setter)` | Add string field |
| `boolField(name, getter, setter)` | Add boolean field |
| `build()` | Build the codec |

---

## ECS Queries

**Package:** `dev.betrix.hytale.kotlin.ecs.query`

### Query Operators

| Operator | Usage | Description |
|----------|-------|-------------|
| `and` | `query1 and query2` | Combine with AND |
| `or` | `query1 or query2` | Combine with OR |
| `not` | `!query` or `query.not()` | Negate query |

---

## ECS Systems

**Package:** `dev.betrix.hytale.kotlin.ecs.system`

### JavaPlugin Extensions

| Extension | Description |
|-----------|-------------|
| `registerSystem(system, tickRate)` | Register ticking system |
| `registerAsyncSystem(system, tickRate)` | Register async system |

### TypedEntitySystem

| Method | Description |
|--------|-------------|
| `onTick(entity)` | Override to handle entity ticks |

Extend with component types:

```kotlin
class MySystem : TypedEntitySystem<ComponentA, ComponentB>()
```

---

## Events

**Package:** `dev.betrix.hytale.kotlin.events`

### JavaPlugin Extensions

| Extension | Description |
|-----------|-------------|
| `registerEvent<E>(handler)` | Register keyed event handler |
| `registerGlobalEvent<E>(handler)` | Register global event handler |
| `registerEvents(block)` | DSL for multiple event registrations |

### EventRegistrationBuilder

| Method | Description |
|--------|-------------|
| `on<E>(handler)` | Register keyed event |
| `global<E>(handler)` | Register global event |

---

## UI - Pages

**Package:** `dev.betrix.hytale.kotlin.ui.pages`

### UIEventBuilder Extensions

| Extension | Description |
|-----------|-------------|
| `bindActivating(selector, data...)` | Bind click event |
| `bindActivating(selector, locksInterface, data...)` | Bind click with lock |
| `bindValueChanged(selector, data...)` | Bind value change event |
| `bindMouseEntered(selector, data...)` | Bind mouse enter event |
| `bindMouseExited(selector, data...)` | Bind mouse exit event |

### Store Extensions

| Extension | Description |
|-----------|-------------|
| `Store.getPlayer(ref)` | Get player component or null |
| `Store.requirePlayer(ref)` | Get player or throw |

### Interfaces

| Interface | Description |
|-----------|-------------|
| `TypedPageData` | Base for typed page data with `type` property |

---

## Core Utilities

**Package:** `dev.betrix.hytale.kotlin.core`

### Collection Extensions

| Extension | Description |
|-----------|-------------|
| `takeAtMost(n)` | Take up to n elements (safe) |
| `randomOrNull()` | Random element or null if empty |
| `toLines()` | Join strings with newlines |

### Message Extensions

| Extension | Description |
|-----------|-------------|
| `Player.msg(text)` | Send raw message |
| `Player.msgLines(lines...)` | Send multiple lines |
| `Player.msgf(format, args...)` | Send formatted message |

---

## Import Cheat Sheet

```kotlin
// Commands
import dev.betrix.hytale.kotlin.commands.*

// ECS Components
import dev.betrix.hytale.kotlin.ecs.component.*

// ECS Queries
import dev.betrix.hytale.kotlin.ecs.query.*

// ECS Systems
import dev.betrix.hytale.kotlin.ecs.system.*

// Events
import dev.betrix.hytale.kotlin.events.*

// UI Pages
import dev.betrix.hytale.kotlin.ui.pages.*

// Core Utilities
import dev.betrix.hytale.kotlin.core.*
```
