---
title: Item Stack Creation and Inventory Management
description: Kotlin DSLs and extensions for working with Hytale's item system
---

# Items

The items module provides Kotlin DSLs and extensions for working with Hytale's item system. This includes creating item stacks, modifying items immutably, and manipulating inventories.

## Overview

Hytale's `ItemStack` class is immutable - every modification returns a new instance. While this is great for thread safety and predictability, it leads to verbose code. This library provides DSLs that make item manipulation concise and readable.

## Creating Item Stacks

### Simple Creation

For basic item stacks, use the simple factory functions:

```kotlin
// Single item
val sword = itemStack("sword_iron")

// With quantity
val stones = itemStack("stone", 64)
```

### DSL Builder

For more complex items with durability, metadata, or custom properties:

```kotlin
val enchantedSword = itemStack("sword_iron") {
    quantity = 1
    durability = 50.0
    maxDurability = 100.0
    metadata("enchantments", EnchantmentCodec, listOf(Enchantment.SHARPNESS))
}
```

The DSL builder provides several ways to set metadata:

```kotlin
itemStack("custom_item") {
    quantity = 5
    
    // Using KeyedCodec (preferred - includes key and codec)
    metadata(MyDataCodec, myData)
    
    // Using key + codec + value
    metadata("custom_key", Codec.STRING, "custom_value")
    
    // Using raw BsonValue
    metadata("raw_data", BsonInt32(42))
    
    // Using a configuration block
    metadata {
        put("key1", BsonString("value1"))
        put("key2", BsonBoolean(true))
    }
}
```

### Pros

- Clear, readable syntax for complex items
- Type-safe metadata encoding
- Automatic durability initialization from item type

### Cons

- Slight overhead compared to direct constructor calls
- Cannot set `overrideDroppedItemAnimation` in builder (use `ItemStack` constructor)
- No explicit validation for quantity (delegates to ItemStack constructor which throws if <= 0)

## Modifying Item Stacks

Since `ItemStack` is immutable, modifications create new instances. The `modify` extension provides a DSL for this:

```kotlin
// Repair an item
val repairedSword = damagedSword.modify {
    restoreDurability()
}

// Apply damage and add metadata
val usedSword = sword.modify {
    durability = durability - 10.0
    metadata("last_hit", Codec.LONG, System.currentTimeMillis())
}

// Change quantity
val halfStack = fullStack.modify {
    quantity = quantity / 2
}
```

### Available Modifications

| Property/Method | Description |
|-----------------|-------------|
| `quantity` | Get/set the quantity (throws if set to 0) |
| `durability` | Get/set durability (clamped to 0..maxDurability) |
| `maxDurability` | Get the max durability |
| `maxDurability(value)` | Set a new max durability |
| `increaseDurability(amount)` | Add to durability (can be negative) |
| `restoreDurability()` | Restore to max durability |
| `state(state)` | Change item state |
| `metadata(...)` | Set metadata (multiple overloads) |

## Item Stack Extensions

The library provides many extension properties and functions for common operations.

### Null-Safety

```kotlin
// Check if null or empty
if (itemStack.isNullOrEmpty()) {
    // handle no item
}

// Check if present and not empty
if (itemStack.isNotEmpty()) {
    // handle item
}

// Get only if not empty
val item = itemStack.takeIfNotEmpty()
```

### Type Checking

```kotlin
// Check item type by ID
if (item.isType("sword_iron")) { }

// Check if same type as another item
if (item.isSameType(otherItem)) { }

// Check if stackable (same ID, durability, metadata)
if (item canStackWith otherItem) { }

// Check if equivalent type (same ID and metadata, ignores durability)
if (item isEquivalentTo otherItem) { }
```

### Durability

```kotlin
// Get durability as ratio (0.0 to 1.0)
val ratio = item.durabilityRatio

// Get durability as percentage (0 to 100)
val percent = item.durabilityPercent

// Check durability state
if (item.isFullDurability) { }
if (item.isLowDurability(0.25)) { } // Below 25%
if (item.isBroken) { }
if (item.isUnbreakable) { }

// Create modified copies
val damagedItem = item.damaged(10.0)
val repairedItem = item.repaired(10.0)
val fullyRepaired = item.fullyRepaired()
```

### Quantity

```kotlin
// Modify quantity (returns null if result would be <= 0)
val more = item.plusQuantity(5)
val less = item.minusQuantity(5)

// Operator alternatives
val more = item + 5
val less = item - 5
val doubled = item * 2

// Split a stack
val (taken, remaining) = item.split(10)

// Take one item
val (single, rest) = item.takeOne()

// Check quantity
if (item.hasAtLeast(10)) { }
if (item.isFullStack) { }
val space = item.remainingSpace
```

### Metadata Access

```kotlin
// Get metadata with null safety
val enchants = item.getMetadataOrNull(EnchantmentsCodec)
val data = item.getMetadataOrNull("custom_key", MyCodec)

// Get with default
val level = item.getMetadataOrDefault("level", LevelCodec)

// Get with fallback
val name = item.getMetadataOrElse("name", Codec.STRING, "Unknown")

// Get with lazy fallback
val computed = item.getMetadataOrElse("value", Codec.INTEGER) {
    calculateDefaultValue()
}

// Check for metadata key
if (item.hasMetadata("enchantments")) { }
```

### Item Asset Properties

```kotlin
// Access underlying Item asset
val asset = item.itemAsset
val maxStack = item.maxStackSize

// Check item categories
if (item.isWeapon) { }
if (item.isTool) { }
if (item.isArmor) { }
if (item.isUtility) { }
if (item.isGlider) { }

// Block association
if (item.hasBlock) {
    val blockId = item.blockIdOrNull
}
```

## Inventory Extensions

The library provides extensions for the `Inventory` class and `ItemContainer` interface.

### Accessing Items

```kotlin
// Get items from specific sections
val hotbarItem = inventory.getHotbarItem(0)
val storageItem = inventory.getStorageItem(5)
val armorItem = inventory.getArmorItem(2)
val utilityItem = inventory.getUtilityItem(0)
val backpackItem = inventory.getBackpackItem(10)

// Get currently held/active items
val held = inventory.heldItem
val hotbar = inventory.activeHotbarItemOrNull
val utility = inventory.activeUtilityItemOrNull
```

### State Checks

```kotlin
// Check emptiness
if (inventory.isHotbarEmpty) { }
if (inventory.isStorageEmpty) { }
if (inventory.isCompletelyEmpty) { }
if (inventory.hasAnyItems) { }

// Count items
val totalItems = inventory.totalItemCount
val usedSlots = inventory.occupiedSlotCount

// Check held item
if (inventory.isHoldingItem) { }
if (inventory.isHolding("sword_iron")) { }
if (inventory.isHolding { it.isWeapon }) { }
```

### Searching Items

```kotlin
// Find items by predicate
val firstWeapon = inventory.findItem { it.isWeapon }
val allArmor = inventory.findAllItems { it.isArmor }

// Find by ID
val sword = inventory.findItemById("sword_iron")

// Check contents
if (inventory.containsItem("stone")) { }
if (inventory.containsAtLeast("stone", 64)) { }

// Count items
val stoneCount = inventory.countItems("stone")
val weaponCount = inventory.countItems { it.isWeapon }
```

### Section Constants

```kotlin
// Use named constants for section IDs
val section = inventory.getSectionOrNull(InventorySections.HOTBAR)
val armor = inventory.getSectionOrNull(InventorySections.ARMOR)
val storage = inventory.getSectionOrNull(InventorySections.STORAGE)
```

Available constants:
- `InventorySections.HOTBAR`
- `InventorySections.STORAGE`
- `InventorySections.ARMOR`
- `InventorySections.UTILITY`
- `InventorySections.TOOLS`
- `InventorySections.BACKPACK`

## ItemContainer Extensions

Extensions for working with any `ItemContainer` (hotbar, storage, armor, etc.):

```kotlin
val container = inventory.hotbar

// State checks
if (container.isEmpty) { }
if (container.hasEmptySlots) { }
val itemCount = container.itemCount
val usedSlots = container.occupiedSlotCount
val freeSlots = container.emptySlotCount

// Iteration
container.forEachItem { item ->
    println(item.itemId)
}

container.forEachSlot { index, item ->
    println("Slot $index: ${item?.itemId ?: "empty"}")
}

// Searching
val weapon = container.findFirst { it.isWeapon }
val weaponSlot = container.findSlot { it.isWeapon }
val allWeapons = container.findAll { it.isWeapon }

// Convert to list
val items = container.toItemList()

// Check contents
if (container.contains("sword_iron")) { }

// Find empty slots
val emptySlot = container.firstEmptySlot()

// Indexed access
val item = container.getOrNull(5)
val item = container[5] // operator syntax
```

## Complete Example

Here's a real-world example combining multiple features:

```kotlin
class LootCommand(
    private val lootTable: LootTable
) {
    val command = playerCommand("loot", "Give random loot") {
        executeWithStore { player, context, store, ref ->
            val inventory = store.getComponent(ref, inventoryType)
                ?: return@executeWithStore context.msg("No inventory found!")
            
            // Check for space
            if (!inventory.hotbar.hasEmptySlots && !inventory.storage.hasEmptySlots) {
                context.msg("Your inventory is full!")
                return@executeWithStore
            }
            
            // Generate loot item
            val lootItem = itemStack(lootTable.randomItemId()) {
                quantity = (1..5).random()
                
                // 10% chance for enchantment
                if (Math.random() < 0.1) {
                    metadata("enchantments", EnchantmentCodec, listOf(
                        Enchantment.entries.random()
                    ))
                }
            }
            
            // Add to inventory
            inventory.combinedHotbarFirst.addItemStack(lootItem)
            context.msg("You received ${lootItem.quantity}x ${lootItem.itemId}!")
        }
    }
}

class DurabilitySystem(
    private val inventoryType: ComponentType<EntityStore, InventoryComponent>
) : EntityTickingSystem<EntityStore>() {
    
    override fun tick(dt: Float, index: Int, chunk: ArchetypeChunk<EntityStore>,
                      store: Store<EntityStore>, buffer: CommandBuffer<EntityStore>) {
        val inventory = chunk.getComponentOrNull(index, inventoryType) ?: return
        val heldItem = inventory.heldItem ?: return
        
        // Check if item is about to break
        if (heldItem.isLowDurability(0.1)) {
            // Notify player (in a real system, you'd send this to the player)
            println("Warning: ${heldItem.itemId} is about to break!")
        }
        
        // Check if item is broken
        if (heldItem.isBroken) {
            // Handle broken item
            val slot = inventory.activeHotbarSlot
            inventory.hotbar.setItemStack(slot, null)
        }
    }
}
```

## Comparison with Raw Hytale API

### Creating Items

```kotlin
// Hytale API
val stack = ItemStack("sword_iron", 1, null)
val withDurability = stack.withDurability(50.0)
val withMetadata = withDurability.withMetadata("key", Codec.STRING, "value")

// Kotlin Library
val stack = itemStack("sword_iron") {
    durability = 50.0
    metadata("key", Codec.STRING, "value")
}
```

### Checking Items

```kotlin
// Hytale API
if (ItemStack.isEmpty(stack) || stack == null) { }
val durabilityRatio = if (stack.maxDurability > 0) 
    stack.durability / stack.maxDurability else 1.0

// Kotlin Library
if (stack.isNullOrEmpty()) { }
val durabilityRatio = stack.durabilityRatio
```

### Iterating Containers

```kotlin
// Hytale API
for (i in 0 until container.capacity) {
    val item = container.getItemStack(i.toShort())
    if (item != null && !item.isEmpty) {
        process(item)
    }
}

// Kotlin Library
container.forEachItem { item ->
    process(item)
}
```

## Best Practices

### 1. Use the DSL for Complex Items

```kotlin
// Good: Clear and readable
val item = itemStack("custom_weapon") {
    quantity = 1
    durability = 100.0
    metadata("damage", DamageCodec, DamageInfo(base = 10, type = DamageType.PHYSICAL))
    metadata("enchantments", EnchantmentCodec, enchants)
}

// Avoid: Hard to read chain
val item = ItemStack("custom_weapon", 1, null)
    .withDurability(100.0)
    .withMetadata("damage", DamageCodec, DamageInfo(base = 10, type = DamageType.PHYSICAL))
    .withMetadata("enchantments", EnchantmentCodec, enchants)
```

### 2. Use Null-Safe Extensions

```kotlin
// Good: Explicit about nullability
val item = inventory.heldItem?.takeIfNotEmpty()

// Good: Check with clear intent
if (stack.isNullOrEmpty()) return
```

### 3. Use Infix Functions for Readability

```kotlin
// Good: Reads like English
if (item canStackWith otherItem) {
    // combine stacks
}

if (item isEquivalentTo template) {
    // items match
}
```

### 4. Prefer Properties Over Methods for State

```kotlin
// Good: Properties for state queries
if (item.isFullDurability) { }
if (item.isWeapon) { }
val ratio = item.durabilityRatio

// Methods for computed values with parameters
if (item.isLowDurability(0.25)) { }
if (item.hasAtLeast(10)) { }
```
