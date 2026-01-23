---
title: ECS Components, Systems, and Queries
description: Kotlin extensions for Hytale's Entity-Component-System architecture
---

# Entity-Component-System (ECS)

The ECS module provides Kotlin extensions for Hytale's Entity-Component-System architecture. This is the core of how entities, their data, and their behavior are managed in Hytale.

## Understanding ECS in Hytale

Hytale uses a data-oriented ECS architecture:

- **Entities** are just IDs (references) with no inherent data or behavior
- **Components** are data containers attached to entities
- **Systems** process entities that have specific components
- **Queries** select which entities a system operates on

This architecture is highly performant because it enables cache-friendly iteration over entities.

## Components

### Creating a Component

Components are simple data classes that extend `Component<EntityStore>`:

```kotlin
class MatchComponent : Component<EntityStore>() {
    var matchId: UUID = UUID.randomUUID()
    var state: MatchState = MatchState.WAITING
    var round: Int = 0
    var players: MutableList<UUID> = mutableListOf()
}

enum class MatchState {
    WAITING, STARTING, IN_PROGRESS, FINISHED
}
```

### Component Codecs with DSL

Hytale requires codecs to serialize/deserialize components. The library provides a type-safe DSL:

```kotlin
class MatchComponent : Component<EntityStore>() {
    var matchId: UUID = UUID.randomUUID()
    var state: MatchState = MatchState.WAITING
    var round: Int = 0
    
    companion object {
        val CODEC = componentCodec(::MatchComponent) {
            uuidField("MatchId", MatchComponent::matchId) { c, v -> c.matchId = v }
            enumField("State", MatchState::class.java, MatchComponent::state) { c, v -> c.state = v }
            intField("Round", MatchComponent::round) { c, v -> c.round = v }
        }
    }
}
```

#### Available Field Types

| Method | Type | Notes |
|--------|------|-------|
| `stringField` | String | Direct string storage |
| `intField` | Int | Integer values |
| `longField` | Long | Large integer values |
| `floatField` | Float | Decimal values |
| `doubleField` | Double | High-precision decimals |
| `boolField` | Boolean | True/false values |
| `uuidField` | UUID | Serialized as string |
| `nullableUuidField` | UUID? | Nullable UUID |
| `enumField` | Enum | Serialized as string name |
| `nullableEnumField` | Enum? | Nullable enum |
| `customField` | Any | Uses a custom Codec |

#### Pros of Codec DSL
- Type-safe field definitions
- Automatic getter/setter inference
- Handles UUID and enum serialization automatically
- Clear, readable syntax

#### Cons of Codec DSL
- Limited to supported field types (use `customField` for others)
- Cannot define complex validation logic inline
- Reflection-based at compile time

### Registering Components

```kotlin
// Using reified generics (no class reference needed)
val matchType = registerComponent<MatchComponent>("Match", MatchComponent.CODEC)

// Or with explicit class
val matchType = registerComponent(
    MatchComponent::class.java, 
    "Match", 
    MatchComponent.CODEC
)

// Register multiple
registerComponents {
    register<MatchComponent>("Match", MatchComponent.CODEC)
    register<ScoreComponent>("Score", ScoreComponent.CODEC)
}
```

## Component Extensions

The library provides Kotlin-idiomatic ways to access components:

### Null-Safe Access

```kotlin
// Explicit null in method name
val match = store.getComponentOrNull(ref, matchType)

// Require throws if not present
val match = store.requireComponent(ref, matchType)

// Check if entity has component
if (store.hasComponent(ref, matchType)) {
    // ...
}

// Execute block if component exists
store.withComponent(ref, matchType) { match ->
    processMatch(match)
}
```

### Chunk Access

When iterating in systems, you work with `ArchetypeChunk`:

```kotlin
// Null-safe access
val match = chunk.getComponentOrNull(index, matchType)

// Require access
val match = chunk.requireComponent(index, matchType)
```

## Systems

### TypedEntitySystem Base Class

The library provides `TypedEntitySystem` that automatically builds queries from component types:

```kotlin
class MatchTickSystem(
    private val matchType: ComponentType<EntityStore, MatchComponent>,
    private val timerType: ComponentType<EntityStore, TimerComponent>
) : TypedEntitySystem(matchType, timerType) {
    
    override fun tick(
        dt: Float,
        index: Int,
        chunk: ArchetypeChunk<EntityStore>,
        store: Store<EntityStore>,
        buffer: CommandBuffer<EntityStore>
    ) {
        val match = chunk.getComponent(index, matchType) ?: return
        val timer = chunk.getComponent(index, timerType) ?: return
        
        timer.remaining -= dt
        if (timer.remaining <= 0) {
            advanceMatchState(match, buffer)
        }
    }
}
```

#### Pros of TypedEntitySystem
- No manual query construction
- Query is built lazily on first access
- Validates that at least one component type is provided

#### Cons of TypedEntitySystem
- Only supports AND queries (all components required)
- Cannot express OR or NOT conditions
- For complex queries, use the query extensions directly

### Registering Systems

```kotlin
// Single system
registerSystem(matchTickSystem)

// Multiple systems
registerSystems(matchTickSystem, scoreSystem, respawnSystem)

// DSL block
registerSystems {
    +MatchTickSystem(matchType, timerType)
    +ScoreUpdateSystem(scoreType)
    +RespawnSystem(healthType, respawnType)
}
```

## Queries

Queries determine which entities a system processes. The library provides operators for building queries:

### Query Operators

```kotlin
// AND query - entities with both components
val query = transformType and velocityType

// Chain multiple ANDs
val query = transformType and velocityType and playerType

// OR query - entities with either component
val query = npcType or playerType

// NOT query - exclude entities with component
val query = transformType and !intangibleType

// Complex combinations
val query = (transformType and velocityType) and !queryOr(intangibleType, invisibleType)
```

### Query Functions

```kotlin
// Create queries from varargs
val andQuery = queryAnd(transformType, velocityType, playerType)
val orQuery = queryOr(npcType, playerType, mobType)
val notQuery = queryNot(intangibleType)
```

### Practical Example

```kotlin
class DamageSystem(
    private val healthType: ComponentType<EntityStore, HealthComponent>,
    private val damageType: ComponentType<EntityStore, DamageComponent>,
    private val invincibleType: ComponentType<EntityStore, InvincibleComponent>
) : EntityTickingSystem<EntityStore>() {
    
    // Entities with health and damage, but NOT invincible
    private val query = (healthType and damageType) and !invincibleType
    
    override fun getQuery() = query
    
    override fun tick(dt: Float, index: Int, chunk: ArchetypeChunk<EntityStore>,
                      store: Store<EntityStore>, buffer: CommandBuffer<EntityStore>) {
        val health = chunk.requireComponent(index, healthType)
        val damage = chunk.requireComponent(index, damageType)
        
        health.current -= damage.amount
        buffer.removeComponent(chunk.getRef(index), damageType)
    }
}
```

## Comparison with Raw Hytale API

### Component Access

```kotlin
// Hytale API
val component = store.getComponent(ref, componentType)
if (component == null) {
    // handle missing
}

// Kotlin Library
val component = store.getComponentOrNull(ref, componentType)
    ?: return handleMissing()

// Or for required components
val component = store.requireComponent(ref, componentType) // throws if missing
```

### Query Building

```kotlin
// Hytale API
val query = Query.and(Query.and(typeA, typeB), Query.not(typeC))

// Kotlin Library
val query = typeA and typeB and !typeC
```

### Codec Definition

```kotlin
// Hytale API
val CODEC = BuilderCodec.builder(MyComponent::class.java, ::MyComponent)
    .append(KeyedCodec("Field1", Codec.STRING), { c, v -> c.field1 = v }, { c -> c.field1 })
    .add()
    .append(KeyedCodec("Field2", Codec.INTEGER), { c, v -> c.field2 = v }, { c -> c.field2 })
    .add()
    .build()

// Kotlin Library
val CODEC = componentCodec(::MyComponent) {
    stringField("Field1", MyComponent::field1) { c, v -> c.field1 = v }
    intField("Field2", MyComponent::field2) { c, v -> c.field2 = v }
}
```

## Best Practices

### 1. Keep Components Small

Components should hold related data only. Split large components:

```kotlin
// Bad: One huge component
class PlayerDataComponent : Component<EntityStore>() {
    var health: Float = 100f
    var mana: Float = 100f
    var inventory: List<Item> = listOf()
    var achievements: Set<String> = setOf()
    var settings: PlayerSettings = PlayerSettings()
    // ... many more fields
}

// Good: Split into focused components
class HealthComponent : Component<EntityStore>() {
    var current: Float = 100f
    var max: Float = 100f
}

class ManaComponent : Component<EntityStore>() {
    var current: Float = 100f
    var max: Float = 100f
}
```

### 2. Use requireComponent for Expected Data

When you know a component should exist (e.g., in a query-matched system):

```kotlin
// The query guarantees these exist
val health = chunk.requireComponent(index, healthType)
val transform = chunk.requireComponent(index, transformType)
```

### 3. Prefer TypedEntitySystem for Simple Cases

For systems that just need all specified components:

```kotlin
// Simple and clear
class RegenerationSystem(
    private val healthType: ComponentType<EntityStore, HealthComponent>,
    private val regenType: ComponentType<EntityStore, RegenerationComponent>
) : TypedEntitySystem(healthType, regenType) {
    // Query is automatic
}
```

### 4. Use Query Operators for Complex Filtering

```kotlin
class CombatSystem(/* ... */) : EntityTickingSystem<EntityStore>() {
    
    // Targets that can be damaged:
    // - Has health and transform
    // - Is not invincible or invisible
    private val damageableQuery = (healthType and transformType) and 
                                   !(invincibleType or invisibleType)
    
    override fun getQuery() = damageableQuery
}
```
