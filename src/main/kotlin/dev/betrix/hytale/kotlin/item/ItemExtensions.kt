@file:JvmName("ItemExtensions")

package dev.betrix.hytale.kotlin.item

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.server.core.asset.type.item.config.Item
import com.hypixel.hytale.server.core.inventory.ItemStack

// ============================================================================
// Null-safe ItemStack checks
// ============================================================================

/**
 * Checks if this item stack is null or empty.
 *
 * @return true if the stack is null or represents an empty item
 */
public fun ItemStack?.isNullOrEmpty(): Boolean = this == null || this.isEmpty

/**
 * Checks if this item stack is not null and not empty.
 *
 * @return true if the stack exists and is not empty
 */
public fun ItemStack?.isNotEmpty(): Boolean = this != null && !this.isEmpty

/**
 * Returns this item stack if it's not empty, otherwise null.
 *
 * @return This stack or null if empty
 */
public fun ItemStack.takeIfNotEmpty(): ItemStack? = if (isEmpty) null else this

// ============================================================================
// Type checking extensions
// ============================================================================

/**
 * Checks if this item is of the specified item type.
 *
 * @param itemId The item ID to check against
 * @return true if the item IDs match
 */
public fun ItemStack.isType(itemId: String): Boolean = this.itemId == itemId

/**
 * Checks if this item is of the same type as another stack.
 *
 * @param other The other item stack to compare
 * @return true if both stacks have the same item ID
 */
public fun ItemStack.isSameType(other: ItemStack?): Boolean =
    ItemStack.isSameItemType(this, other)

/**
 * Checks if this item can be stacked with another.
 * Items must have the same ID, durability, and metadata to stack.
 *
 * @param other The other item stack to check
 * @return true if the items can be combined in a stack
 */
public infix fun ItemStack.canStackWith(other: ItemStack?): Boolean =
    isStackableWith(other)

/**
 * Checks if this item is equivalent in type to another.
 * Compares item ID and metadata, but not durability.
 *
 * @param other The other item stack to check
 * @return true if the items are equivalent in type
 */
public infix fun ItemStack.isEquivalentTo(other: ItemStack?): Boolean =
    isEquivalentType(other)

// ============================================================================
// Durability extensions
// ============================================================================

/**
 * The durability ratio of this item (current / max).
 * Returns 1.0 for unbreakable items.
 */
public val ItemStack.durabilityRatio: Double
    get() = if (isUnbreakable) 1.0 else durability / maxDurability

/**
 * The percentage of durability remaining (0-100).
 * Returns 100.0 for unbreakable items.
 */
public val ItemStack.durabilityPercent: Double
    get() = durabilityRatio * 100.0

/**
 * Checks if the item is at full durability.
 *
 * @return true if durability equals max durability or item is unbreakable
 */
public val ItemStack.isFullDurability: Boolean
    get() = isUnbreakable || durability >= maxDurability

/**
 * Checks if the item is low on durability (below threshold).
 *
 * @param threshold The ratio threshold (default 0.25 = 25%)
 * @return true if durability ratio is below the threshold
 */
public fun ItemStack.isLowDurability(threshold: Double = 0.25): Boolean =
    !isUnbreakable && durabilityRatio < threshold

/**
 * Creates a copy of this item with damage applied.
 *
 * @param damage The amount of durability to remove
 * @return A new item stack with reduced durability
 */
public fun ItemStack.damaged(damage: Double): ItemStack =
    withIncreasedDurability(-damage)

/**
 * Creates a copy of this item with repairs applied.
 *
 * @param amount The amount of durability to restore
 * @return A new item stack with increased durability
 */
public fun ItemStack.repaired(amount: Double): ItemStack =
    withIncreasedDurability(amount)

/**
 * Creates a copy of this item with full durability restored.
 *
 * @return A new item stack at max durability
 */
public fun ItemStack.fullyRepaired(): ItemStack =
    withRestoredDurability(maxDurability)

// ============================================================================
// Quantity extensions
// ============================================================================

/**
 * Creates a copy with increased quantity.
 *
 * @param amount The amount to add
 * @return A new item stack with increased quantity, or null if result would be <= 0
 */
public fun ItemStack.plusQuantity(amount: Int): ItemStack? =
    withQuantity(quantity + amount)

/**
 * Creates a copy with decreased quantity.
 *
 * @param amount The amount to remove
 * @return A new item stack with decreased quantity, or null if result would be <= 0
 */
public fun ItemStack.minusQuantity(amount: Int): ItemStack? =
    withQuantity(quantity - amount)

/**
 * Splits this stack, returning a pair of (taken, remaining).
 *
 * @param amount The amount to take
 * @return A pair of (taken stack, remaining stack or null if empty)
 */
public fun ItemStack.split(amount: Int): Pair<ItemStack?, ItemStack?> {
    require(amount >= 0) { "Split amount must be non-negative" }
    val taken = withQuantity(minOf(amount, quantity))
    val remaining = withQuantity(quantity - amount)
    return taken to remaining
}

/**
 * Takes one item from this stack.
 *
 * @return A pair of (single item, remaining stack or null if empty)
 */
public fun ItemStack.takeOne(): Pair<ItemStack, ItemStack?> {
    val single = withQuantity(1) ?: error("Cannot create stack with quantity 1")
    val remaining = withQuantity(quantity - 1)
    return single to remaining
}

/**
 * Checks if this stack has at least the specified quantity.
 *
 * @param amount The minimum quantity required
 * @return true if quantity >= amount
 */
public fun ItemStack.hasAtLeast(amount: Int): Boolean = quantity >= amount

/**
 * Checks if the stack is full (at max stack size for its item type).
 *
 * @return true if quantity equals the item's max stack size
 */
public val ItemStack.isFullStack: Boolean
    get() = quantity >= item.maxStack

/**
 * The remaining space in this stack before it's full.
 *
 * @return The number of items that can be added
 */
public val ItemStack.remainingSpace: Int
    get() = (item.maxStack - quantity).coerceAtLeast(0)

// ============================================================================
// Metadata extensions
// ============================================================================

/**
 * Gets a metadata value or null if not present.
 *
 * @param keyedCodec The keyed codec for the metadata
 * @return The decoded value or null
 */
public fun <T> ItemStack.getMetadataOrNull(keyedCodec: KeyedCodec<T>): T? =
    getFromMetadataOrNull(keyedCodec)

/**
 * Gets a metadata value or null if not present.
 *
 * @param key The metadata key
 * @param codec The codec to decode the value
 * @return The decoded value or null
 */
public fun <T> ItemStack.getMetadataOrNull(key: String, codec: Codec<T>): T? =
    getFromMetadataOrNull(key, codec)

/**
 * Gets a metadata value or a default if not present.
 *
 * @param key The metadata key
 * @param codec The builder codec with default value
 * @return The decoded value or the codec's default
 */
public fun <T> ItemStack.getMetadataOrDefault(key: String, codec: BuilderCodec<T>): T =
    getFromMetadataOrDefault(key, codec)

/**
 * Gets a metadata value or the provided default if not present.
 *
 * @param key The metadata key
 * @param codec The codec to decode the value
 * @param default The default value to return if not present
 * @return The decoded value or the default
 */
public fun <T> ItemStack.getMetadataOrElse(key: String, codec: Codec<T>, default: T): T =
    getFromMetadataOrNull(key, codec) ?: default

/**
 * Gets a metadata value or computes a default if not present.
 *
 * @param key The metadata key
 * @param codec The codec to decode the value
 * @param defaultProvider The function to compute the default value
 * @return The decoded value or the computed default
 */
public inline fun <T> ItemStack.getMetadataOrElse(
    key: String,
    codec: Codec<T>,
    defaultProvider: () -> T
): T = getFromMetadataOrNull(key, codec) ?: defaultProvider()

/**
 * Checks if this item has metadata with the given key.
 *
 * @param key The metadata key to check
 * @return true if the key exists in metadata
 */
public fun ItemStack.hasMetadata(key: String): Boolean =
    metadata?.containsKey(key) == true

// ============================================================================
// Item asset extensions
// ============================================================================

/**
 * The underlying [Item] asset for this stack.
 * Returns [Item.UNKNOWN] if the item type is not found.
 */
public val ItemStack.itemAsset: Item
    get() = item

/**
 * The maximum stack size for this item type.
 */
public val ItemStack.maxStackSize: Int
    get() = item.maxStack

/**
 * Checks if this item has an associated block type.
 */
public val ItemStack.hasBlock: Boolean
    get() = item.hasBlockType()

/**
 * The block ID associated with this item, or null if none.
 */
public val ItemStack.blockIdOrNull: String?
    get() = if (item.hasBlockType()) item.blockId else null

/**
 * Checks if this item is a weapon.
 */
public val ItemStack.isWeapon: Boolean
    get() = item.weapon != null

/**
 * Checks if this item is a tool.
 */
public val ItemStack.isTool: Boolean
    get() = item.tool != null

/**
 * Checks if this item is armor.
 */
public val ItemStack.isArmor: Boolean
    get() = item.armor != null

/**
 * Checks if this item is a utility item.
 */
public val ItemStack.isUtility: Boolean
    get() = item.utility?.isUsable == true

/**
 * Checks if this item is a glider.
 */
public val ItemStack.isGlider: Boolean
    get() = item.glider != null

// ============================================================================
// Operator overloads
// ============================================================================

/**
 * Creates a copy with quantity multiplied.
 * The result is clamped to [1, maxStack].
 *
 * @param multiplier The multiplier
 * @return A new item stack with multiplied quantity
 */
public operator fun ItemStack.times(multiplier: Int): ItemStack {
    val newQuantity = (quantity * multiplier).coerceIn(1, item.maxStack)
    return withQuantity(newQuantity) ?: this
}

/**
 * Creates a copy with quantity added.
 *
 * @param amount The amount to add
 * @return A new item stack with increased quantity, or null if result <= 0
 */
public operator fun ItemStack.plus(amount: Int): ItemStack? =
    plusQuantity(amount)

/**
 * Creates a copy with quantity subtracted.
 *
 * @param amount The amount to subtract
 * @return A new item stack with decreased quantity, or null if result <= 0
 */
public operator fun ItemStack.minus(amount: Int): ItemStack? =
    minusQuantity(amount)

// ============================================================================
// Comparison extensions
// ============================================================================

/**
 * Compares item stacks by quantity.
 */
public fun ItemStack.compareQuantityTo(other: ItemStack): Int =
    quantity.compareTo(other.quantity)

/**
 * Compares item stacks by durability.
 */
public fun ItemStack.compareDurabilityTo(other: ItemStack): Int =
    durability.compareTo(other.durability)
