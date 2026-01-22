@file:JvmName("InventoryExtensions")

package dev.betrix.hytale.kotlin.item

import com.hypixel.hytale.server.core.inventory.Inventory
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.inventory.container.ItemContainer

// ============================================================================
// Inventory section accessors
// ============================================================================

/**
 * Gets the item stack at the specified slot in the hotbar.
 *
 * @param slot The slot index (0-8 by default)
 * @return The item stack at that slot, or null if empty
 */
public fun Inventory.getHotbarItem(slot: Int): ItemStack? =
    hotbar.getItemStack(slot.toShort())?.takeIfNotEmpty()

/**
 * Gets the item stack at the specified slot in the storage.
 *
 * @param slot The slot index (0-35 by default)
 * @return The item stack at that slot, or null if empty
 */
public fun Inventory.getStorageItem(slot: Int): ItemStack? =
    storage.getItemStack(slot.toShort())?.takeIfNotEmpty()

/**
 * Gets the item stack at the specified armor slot.
 *
 * @param slot The armor slot index
 * @return The item stack at that slot, or null if empty
 */
public fun Inventory.getArmorItem(slot: Int): ItemStack? =
    armor.getItemStack(slot.toShort())?.takeIfNotEmpty()

/**
 * Gets the item stack at the specified utility slot.
 *
 * @param slot The utility slot index (0-3 by default)
 * @return The item stack at that slot, or null if empty
 */
public fun Inventory.getUtilityItem(slot: Int): ItemStack? =
    utility.getItemStack(slot.toShort())?.takeIfNotEmpty()

/**
 * Gets the item stack at the specified backpack slot.
 *
 * @param slot The backpack slot index
 * @return The item stack at that slot, or null if empty
 */
public fun Inventory.getBackpackItem(slot: Int): ItemStack? =
    backpack.getItemStack(slot.toShort())?.takeIfNotEmpty()

// ============================================================================
// Inventory state checks
// ============================================================================

/**
 * Checks if the hotbar is completely empty.
 */
public val Inventory.isHotbarEmpty: Boolean
    get() = hotbar.isEmpty

/**
 * Checks if the storage is completely empty.
 */
public val Inventory.isStorageEmpty: Boolean
    get() = storage.isEmpty

/**
 * Checks if all sections of the inventory are empty.
 */
public val Inventory.isCompletelyEmpty: Boolean
    get() = hotbar.isEmpty && storage.isEmpty && armor.isEmpty && utility.isEmpty && backpack.isEmpty

/**
 * Checks if any section of the inventory contains items.
 */
public val Inventory.hasAnyItems: Boolean
    get() = !isCompletelyEmpty

/**
 * The total number of items across all inventory sections.
 */
public val Inventory.totalItemCount: Int
    get() = hotbar.itemCount + storage.itemCount + armor.itemCount +
            utility.itemCount + backpack.itemCount

/**
 * The total number of occupied slots across all sections.
 */
public val Inventory.occupiedSlotCount: Int
    get() = hotbar.occupiedSlotCount + storage.occupiedSlotCount + armor.occupiedSlotCount +
            utility.occupiedSlotCount + backpack.occupiedSlotCount

// ============================================================================
// Active slot helpers
// ============================================================================

/**
 * The currently held item (from active hotbar or tools slot).
 * Alias for [Inventory.getItemInHand].
 */
public val Inventory.heldItem: ItemStack?
    get() = itemInHand?.takeIfNotEmpty()

/**
 * The item in the active hotbar slot.
 */
public val Inventory.activeHotbarItemOrNull: ItemStack?
    get() = activeHotbarItem?.takeIfNotEmpty()

/**
 * The item in the active utility slot.
 */
public val Inventory.activeUtilityItemOrNull: ItemStack?
    get() = utilityItem?.takeIfNotEmpty()

/**
 * Checks if the player is holding an item.
 */
public val Inventory.isHoldingItem: Boolean
    get() = heldItem != null

/**
 * Checks if the player is holding a specific item type.
 *
 * @param itemId The item ID to check for
 * @return true if holding an item with the specified ID
 */
public fun Inventory.isHolding(itemId: String): Boolean =
    heldItem?.itemId == itemId

/**
 * Checks if the player is holding any item matching the predicate.
 *
 * @param predicate The condition to check
 * @return true if holding an item that matches
 */
public inline fun Inventory.isHolding(predicate: (ItemStack) -> Boolean): Boolean =
    heldItem?.let(predicate) == true

// ============================================================================
// Item searching
// ============================================================================

/**
 * Finds the first item matching the predicate across all inventory sections.
 *
 * @param predicate The condition to match
 * @return The first matching item stack, or null if not found
 */
public inline fun Inventory.findItem(predicate: (ItemStack) -> Boolean): ItemStack? {
    return combinedEverything.findFirst(predicate)
}

/**
 * Finds all items matching the predicate across all inventory sections.
 *
 * @param predicate The condition to match
 * @return A list of all matching item stacks
 */
public inline fun Inventory.findAllItems(predicate: (ItemStack) -> Boolean): List<ItemStack> {
    return combinedEverything.findAll(predicate)
}

/**
 * Finds the first item with the specified item ID.
 *
 * @param itemId The item ID to find
 * @return The first matching item stack, or null if not found
 */
public fun Inventory.findItemById(itemId: String): ItemStack? {
    return combinedEverything.findFirst { it.itemId == itemId }
}

/**
 * Checks if the inventory contains at least one item with the specified ID.
 *
 * @param itemId The item ID to check for
 * @return true if the item exists in the inventory
 */
public fun Inventory.containsItem(itemId: String): Boolean =
    findItemById(itemId) != null

/**
 * Checks if the inventory contains at least the specified quantity of an item.
 *
 * @param itemId The item ID to check for
 * @param quantity The minimum quantity required
 * @return true if the total quantity across all stacks is >= quantity
 */
public fun Inventory.containsAtLeast(itemId: String, quantity: Int): Boolean {
    var total = 0
    combinedEverything.forEachItem { item ->
        if (item.itemId == itemId) {
            total += item.quantity
            if (total >= quantity) return true
        }
    }
    return false
}

/**
 * Counts the total quantity of items with the specified ID.
 *
 * @param itemId The item ID to count
 * @return The total quantity across all stacks
 */
public fun Inventory.countItems(itemId: String): Int {
    var total = 0
    combinedEverything.forEachItem { item ->
        if (item.itemId == itemId) {
            total += item.quantity
        }
    }
    return total
}

/**
 * Counts the total quantity of items matching the predicate.
 *
 * @param predicate The condition to match
 * @return The total quantity across all matching stacks
 */
public inline fun Inventory.countItems(predicate: (ItemStack) -> Boolean): Int {
    var total = 0
    combinedEverything.forEachItem { item ->
        if (predicate(item)) {
            total += item.quantity
        }
    }
    return total
}

// ============================================================================
// Section ID helpers
// ============================================================================

/**
 * Constants for inventory section IDs.
 */
public object InventorySections {
    public const val HOTBAR: Int = Inventory.HOTBAR_SECTION_ID
    public const val STORAGE: Int = Inventory.STORAGE_SECTION_ID
    public const val ARMOR: Int = Inventory.ARMOR_SECTION_ID
    public const val UTILITY: Int = Inventory.UTILITY_SECTION_ID
    public const val TOOLS: Int = Inventory.TOOLS_SECTION_ID
    public const val BACKPACK: Int = Inventory.BACKPACK_SECTION_ID
}

/**
 * Gets a section by its ID with null safety.
 *
 * @param sectionId The section ID constant
 * @return The item container for that section, or null if invalid
 */
public fun Inventory.getSectionOrNull(sectionId: Int): ItemContainer? =
    getSectionById(sectionId)

// ============================================================================
// ItemContainer extensions
// ============================================================================

/**
 * Checks if the container is empty.
 */
public val ItemContainer.isEmpty: Boolean
    get() {
        for (i in 0 until capacity) {
            val item = getItemStack(i.toShort())
            if (item != null && !item.isEmpty) return false
        }
        return true
    }

/**
 * Counts the total number of items in this container.
 */
public val ItemContainer.itemCount: Int
    get() {
        var count = 0
        for (i in 0 until capacity) {
            val item = getItemStack(i.toShort())
            if (item != null && !item.isEmpty) {
                count += item.quantity
            }
        }
        return count
    }

/**
 * Counts the number of occupied slots in this container.
 */
public val ItemContainer.occupiedSlotCount: Int
    get() {
        var count = 0
        for (i in 0 until capacity) {
            val item = getItemStack(i.toShort())
            if (item != null && !item.isEmpty) count++
        }
        return count
    }

/**
 * Counts the number of empty slots in this container.
 */
public val ItemContainer.emptySlotCount: Int
    get() = capacity - occupiedSlotCount

/**
 * Checks if the container has any empty slots.
 */
public val ItemContainer.hasEmptySlots: Boolean
    get() = emptySlotCount > 0

/**
 * Iterates over all non-empty items in this container.
 *
 * @param action The action to perform on each item
 */
public inline fun ItemContainer.forEachItem(action: (ItemStack) -> Unit) {
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item != null && !item.isEmpty) {
            action(item)
        }
    }
}

/**
 * Iterates over all slots with their indices.
 *
 * @param action The action to perform on each slot (index, item or null)
 */
public inline fun ItemContainer.forEachSlot(action: (index: Int, item: ItemStack?) -> Unit) {
    for (i in 0 until capacity) {
        action(i, getItemStack(i.toShort()))
    }
}

/**
 * Finds the first item matching the predicate.
 *
 * @param predicate The condition to match
 * @return The first matching item, or null if not found
 */
public inline fun ItemContainer.findFirst(predicate: (ItemStack) -> Boolean): ItemStack? {
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item != null && !item.isEmpty && predicate(item)) {
            return item
        }
    }
    return null
}

/**
 * Finds the first slot index containing an item matching the predicate.
 *
 * @param predicate The condition to match
 * @return The slot index, or -1 if not found
 */
public inline fun ItemContainer.findSlot(predicate: (ItemStack) -> Boolean): Int {
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item != null && !item.isEmpty && predicate(item)) {
            return i
        }
    }
    return -1
}

/**
 * Finds all items matching the predicate.
 *
 * @param predicate The condition to match
 * @return A list of all matching items
 */
public inline fun ItemContainer.findAll(predicate: (ItemStack) -> Boolean): List<ItemStack> {
    val result = mutableListOf<ItemStack>()
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item != null && !item.isEmpty && predicate(item)) {
            result.add(item)
        }
    }
    return result
}

/**
 * Converts the container contents to a list of non-empty item stacks.
 *
 * @return A list of all non-empty items in slot order
 */
public fun ItemContainer.toItemList(): List<ItemStack> {
    val result = mutableListOf<ItemStack>()
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item != null && !item.isEmpty) {
            result.add(item)
        }
    }
    return result
}

/**
 * Checks if this container contains an item with the specified ID.
 *
 * @param itemId The item ID to check for
 * @return true if the item exists in the container
 */
public fun ItemContainer.contains(itemId: String): Boolean =
    findFirst { it.itemId == itemId } != null

/**
 * Gets the first empty slot index.
 *
 * @return The index of the first empty slot, or -1 if full
 */
public fun ItemContainer.firstEmptySlot(): Int {
    for (i in 0 until capacity) {
        val item = getItemStack(i.toShort())
        if (item == null || item.isEmpty) {
            return i
        }
    }
    return -1
}

/**
 * Gets an item from a slot or null if empty.
 *
 * @param slot The slot index
 * @return The item stack or null if empty/out of bounds
 */
public fun ItemContainer.getOrNull(slot: Int): ItemStack? {
    if (slot < 0 || slot >= capacity) return null
    return getItemStack(slot.toShort())?.takeIfNotEmpty()
}

/**
 * Indexed access operator for getting items.
 *
 * @param slot The slot index
 * @return The item stack or null if empty
 */
public operator fun ItemContainer.get(slot: Int): ItemStack? = getOrNull(slot)
