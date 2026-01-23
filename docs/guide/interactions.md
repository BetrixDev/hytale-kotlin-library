---
title: Hytale Interaction Assets and Handlers
description: Kotlin DSLs for building Hytale interaction assets and assigning them to entities
---

# Interactions

The interactions module provides Kotlin DSLs for building Hytale interaction assets and assigning them to entities. This wraps the server interaction configuration classes with a more fluent, type-safe builder API.

## Overview

Hytale's interaction assets are configured through `Interaction` and `RootInteraction` classes, but they expose mostly protected fields. The DSL here builds the same objects with an ergonomic Kotlin API and keeps the runtime behavior identical to the vanilla server code.

## Building Interactions

### Simple Interaction

```kotlin
val windUp = simpleInteraction("MyPlugin:WindUp") {
    runTime = 0.35f
    cancelOnItemChange = true
    effects {
        itemAnimationId = "WindUp"
        waitForAnimationToFinish = true
    }
    next("MyPlugin:Strike")
    failed("MyPlugin:StrikeFail")
}
```

### Serial and Parallel Chains

```kotlin
val comboChain = serialInteraction("MyPlugin:ComboChain") {
    interactions("MyPlugin:WindUp", "MyPlugin:Strike", "MyPlugin:Recover")
}

val parallelChain = parallelInteraction("MyPlugin:ParallelFx") {
    roots("MyPlugin:DamageRoot", "MyPlugin:SoundRoot")
}
```

### Repeating Chains

```kotlin
val comboLoop = repeatInteraction("MyPlugin:ComboLoop") {
    forkRoot("MyPlugin:ComboRoot")
    repeat(3)
}
```

## Root Interactions

Root interactions are entry points for interaction chains. Use them to wire up cooldowns, rules, and per-gamemode settings.

```kotlin
val comboRoot = rootInteraction("MyPlugin:ComboRoot") {
    interactions("MyPlugin:ComboChain")
    requireNewClick = true
    clickQueuingTimeout = 0.2f

    cooldown {
        id = "combo"
        cooldown = 0.9f
        clickBypass = false
    }

    settings(GameMode.Adventure) {
        allowSkipChainOnClick = true
    }
}
```

## Effects, Rules, and Camera Settings

```kotlin
val impact = simpleInteraction("MyPlugin:Impact") {
    effects {
        worldSoundEventId = "MyPlugin:ImpactSound"
        clearAnimationOnFinish = true
    }

    rules {
        blocking(InteractionType.Primary, InteractionType.Secondary)
        interrupting(InteractionType.Dodge)
        blockingBypassTag = "legendary"
    }

    camera {
        firstPerson {
            camera(
                time = 0.2f,
                position = Vector3f(0.0f, 0.1f, -0.15f),
                rotation = Direction(3.0f, 0.0f, 0.0f)
            )
        }
    }
}
```

## Assigning Interactions to Entities

`Interactions` components map an `InteractionType` to a root interaction ID.

```kotlin
val interactionComponent = interactions {
    hint = "Combo"
    type(InteractionType.Primary, "MyPlugin:ComboRoot")
    type(InteractionType.Secondary, "MyPlugin:ParryRoot")
}
```

Attach the component via your normal ECS component registration flow.

## Interaction Handlers (Custom Logic)

The interaction handler wrapper hooks into interaction events and lets you attach Kotlin logic, while still starting root interactions when needed.

```kotlin
registerInteractionHandlers {
    on(InteractionType.Primary) {
        filter { itemInHand?.itemId == "MyPlugin:ComboBlade" }
        handle {
            startRoot("MyPlugin:ComboRoot")
            stopPropagation()
        }
    }

    onAny {
        filter { targetEntity != null && type == InteractionType.Secondary }
        handle {
            cancel()
            startRoot("MyPlugin:InspectRoot")
        }
    }
}
```

You can also forward to whatever the player would normally use:

```kotlin
registerInteractionHandlers {
    on(InteractionType.Primary) {
        filter { itemInHand?.itemId == "MyPlugin:Wand" }
        handle {
            startDefaultRoot()
        }
    }
}
```

## Registering Assets

The DSL builds the same objects that the asset stores expect. Register them like any other runtime assets:

```kotlin
Interaction.getAssetStore().loadAssets("MyPlugin", listOf(windUp, comboChain, comboLoop, impact))
RootInteraction.getAssetStore().loadAssets("MyPlugin", listOf(comboRoot))
```

## Notes

- The DSL uses reflection under the hood to set protected configuration fields.
- Only a subset of interaction types is wrapped today (Simple, Serial, Parallel, Repeat, Root).
- Interaction handlers use `PlayerInteractEvent`, which is marked deprecated in the server API.
- You can still mix DSL-built assets with JSON-defined assets from your mod data.
