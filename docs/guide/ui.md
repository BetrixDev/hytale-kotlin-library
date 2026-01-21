# UI

The UI module provides Kotlin extensions for working with Hytale's page event bindings, simplifying how you wire up UI interactions.

## Overview

Hytale allows servers to display custom UI pages to players - full-screen or partial views like menus, inventories, and dialogs. The library provides extensions that simplify event binding with a more Kotlin-idiomatic syntax.

## Page Event Bindings

Pages in Hytale can respond to UI events like button clicks, value changes, and mouse interactions. The library simplifies event binding with extension functions.

### Activating Events (Clicks)

Bind click/activation events to UI elements:

```kotlin
eventBuilder.bindActivating("#SkeletonButton", 
    "Type" to "SelectKit", 
    "KitId" to "skeleton"
)

eventBuilder.bindActivating("#ClearKit", 
    "Type" to "ClearKit"
)

eventBuilder.bindActivating("#PlayButton",
    "Type" to "StartGame",
    "Mode" to gameMode,
    "Difficulty" to difficulty
)
```

#### With Interface Locking

Some actions should lock the UI to prevent double-clicks:

```kotlin
eventBuilder.bindActivating("#ConfirmPurchase", 
    locksInterface = true,
    "Type" to "Purchase",
    "ItemId" to itemId
)
```

### Value Changed Events

React to input changes in text fields, sliders, etc.:

```kotlin
eventBuilder.bindValueChanged("#SearchInput", 
    "@SearchQuery" to "#SearchInput.Value"
)

eventBuilder.bindValueChanged("#VolumeSlider",
    "Type" to "VolumeChanged",
    "@Volume" to "#VolumeSlider.Value"
)
```

### Mouse Events

Handle hover states for tooltips and visual feedback:

```kotlin
eventBuilder.bindMouseEntered("#ItemSlot",
    "Type" to "ShowTooltip",
    "SlotId" to slotId
)

eventBuilder.bindMouseExited("#ItemSlot",
    "Type" to "HideTooltip"
)
```

## Player Access in Pages

When handling page events, you often need to access the player entity. The library provides convenient accessors:

```kotlin
// Returns null if not found
val player = store.getPlayer(ref) ?: return sendUpdate()

// Throws if not found
val player = store.requirePlayer(ref)
```

## Typed Page Data Pattern

For type-safe event handling, implement the `TypedPageData` interface:

```kotlin
class KitSelectionData : TypedPageData {
    override var type: String = ""
    var kitId: String? = null
    var variant: String? = null
}

// In your page handler:
override fun handleDataEvent(
    store: Store<EntityStore>,
    ref: Ref<EntityStore>,
    data: KitSelectionData
) {
    when (data.type) {
        "SelectKit" -> handleSelectKit(data.kitId!!, data.variant)
        "ClearKit" -> handleClearKit()
        "Preview" -> handlePreview(data.kitId!!)
    }
}
```

## Comparison with Raw Hytale API

```kotlin
// Hytale API - requires creating EventData manually
val eventData = EventData()
eventData.append("Type", "SelectKit")
eventData.append("KitId", "skeleton")
eventBuilder.addEventBinding(
    CustomUIEventBindingType.Activating, 
    "#SkeletonButton", 
    eventData, 
    false
)

// Kotlin Library - pairs syntax
eventBuilder.bindActivating("#SkeletonButton",
    "Type" to "SelectKit",
    "KitId" to "skeleton"
)
```

## Practical Examples

### Kit Selection Page

```kotlin
class KitSelectionPage : CustomUIPage<KitSelectionData>() {
    
    override fun buildEvents(builder: UIEventBuilder) {
        // Kit buttons
        for (kit in availableKits) {
            builder.bindActivating("#${kit.id}Button",
                "Type" to "SelectKit",
                "KitId" to kit.id
            )
        }
        
        // Clear selection
        builder.bindActivating("#ClearButton", "Type" to "ClearKit")
        
        // Confirm and close
        builder.bindActivating("#ConfirmButton",
            locksInterface = true,
            "Type" to "Confirm"
        )
    }
    
    override fun handleDataEvent(
        store: Store<EntityStore>,
        ref: Ref<EntityStore>,
        data: KitSelectionData
    ) {
        val player = store.requirePlayer(ref)
        
        when (data.type) {
            "SelectKit" -> {
                val kitId = data.kitId ?: return
                selectKit(player, kitId)
                updateSelectionDisplay(kitId)
            }
            "ClearKit" -> {
                clearSelection(player)
                updateSelectionDisplay(null)
            }
            "Confirm" -> {
                confirmSelection(player)
                closePage(player)
            }
        }
    }
}
```

### Settings Page with Inputs

```kotlin
class SettingsPage : CustomUIPage<SettingsData>() {
    
    override fun buildEvents(builder: UIEventBuilder) {
        // Sliders
        builder.bindValueChanged("#VolumeSlider",
            "Type" to "VolumeChanged",
            "@Value" to "#VolumeSlider.Value"
        )
        
        builder.bindValueChanged("#SensitivitySlider",
            "Type" to "SensitivityChanged",
            "@Value" to "#SensitivitySlider.Value"
        )
        
        // Toggle buttons
        builder.bindActivating("#MusicToggle", "Type" to "ToggleMusic")
        builder.bindActivating("#SfxToggle", "Type" to "ToggleSfx")
        
        // Save/Cancel
        builder.bindActivating("#SaveButton", "Type" to "Save")
        builder.bindActivating("#CancelButton", "Type" to "Cancel")
    }
}
```

### Shop Page with Hover Tooltips

```kotlin
class ShopPage : CustomUIPage<ShopData>() {
    
    override fun buildEvents(builder: UIEventBuilder) {
        for (item in shopItems) {
            // Purchase button
            builder.bindActivating("#Buy_${item.id}",
                locksInterface = true,
                "Type" to "Purchase",
                "ItemId" to item.id
            )
            
            // Hover for tooltip
            builder.bindMouseEntered("#Item_${item.id}",
                "Type" to "ShowTooltip",
                "ItemId" to item.id
            )
            
            builder.bindMouseExited("#Item_${item.id}",
                "Type" to "HideTooltip"
            )
        }
    }
}
```

## Pros and Cons

### Pros

| Feature | Benefit |
|---------|---------|
| Pairs syntax | `"Key" to value` is more Kotlin-idiomatic |
| Concise binding | Single line vs multiple statements |
| Type-safe handlers | `TypedPageData` enables when expressions |
| Player accessors | `getPlayer`/`requirePlayer` reduce boilerplate |

### Cons

| Limitation | Context |
|------------|---------|
| String selectors | Selectors are still strings, no compile-time validation |
| Limited event types | Only common binding types are wrapped |

## Best Practices

### 1. Use Constants for Selectors

Avoid magic strings by defining selector constants:

```kotlin
object ShopSelectors {
    const val BUY_PREFIX = "#Buy_"
    const val ITEM_PREFIX = "#Item_"
    const val CONFIRM = "#ConfirmButton"
    const val CANCEL = "#CancelButton"
}
```

### 2. Use TypedPageData for Complex Pages

When a page has multiple action types, implement `TypedPageData`:

```kotlin
class ShopData : TypedPageData {
    override var type: String = ""
    var itemId: String? = null
    var quantity: Int = 1
}
```

### 3. Handle Missing Players Gracefully

Always check for player existence in page handlers:

```kotlin
override fun handleDataEvent(
    store: Store<EntityStore>,
    ref: Ref<EntityStore>,
    data: MyPageData
) {
    val player = store.getPlayer(ref)
    if (player == null) {
        // Player disconnected during interaction
        return sendUpdate()
    }
    
    // Safe to proceed
    processAction(player, data)
}
```

### 4. Use Interface Locking for Destructive Actions

Prevent double-clicks on purchase, delete, or confirm actions:

```kotlin
builder.bindActivating("#DeleteAccount",
    locksInterface = true,
    "Type" to "DeleteAccount"
)
```
