@file:JvmName("ItemStacks")

package com.github.BetrixDev.hytaleKt.item

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.server.core.inventory.ItemStack
import org.bson.BsonDocument
import org.bson.BsonValue

/**
 * DSL marker for item stack builder to prevent nested DSL scope leaking.
 */
@DslMarker
public annotation class ItemDslMarker

/**
 * Builder for creating and modifying [ItemStack] instances using a DSL.
 *
 * Example:
 * ```kotlin
 * val sword = itemStack("sword_iron") {
 *     quantity = 1
 *     durability = 50.0
 *     metadata("enchantments", EnchantmentCodec, myEnchants)
 * }
 * ```
 *
 * @param itemId The item identifier
 */
@ItemDslMarker
public class ItemStackBuilder(private val itemId: String) {
    /**
     * The quantity of items in this stack.
     * Must be greater than 0.
     */
    public var quantity: Int = 1

    /**
     * The current durability of the item.
     * If not set, defaults to the item's max durability.
     */
    public var durability: Double? = null

    /**
     * The maximum durability of the item.
     * If not set, defaults to the item type's max durability.
     */
    public var maxDurability: Double? = null

    private var metadata: BsonDocument? = null

    /**
     * Sets a metadata value using a [KeyedCodec].
     *
     * Example:
     * ```kotlin
     * metadata(EnchantmentsCodec, listOf(Enchantment.SHARPNESS))
     * ```
     *
     * @param keyedCodec The keyed codec containing both the key and encoder
     * @param value The value to encode, or null to remove
     */
    public fun <T> metadata(keyedCodec: KeyedCodec<T>, value: T?) {
        metadata(keyedCodec.key, keyedCodec.childCodec, value)
    }

    /**
     * Sets a metadata value using a key and [Codec].
     *
     * Example:
     * ```kotlin
     * metadata("custom_data", MyDataCodec, myData)
     * ```
     *
     * @param key The metadata key
     * @param codec The codec to encode the value
     * @param value The value to encode, or null to remove
     */
    public fun <T> metadata(key: String, codec: Codec<T>, value: T?) {
        ensureMetadata()
        if (value == null) {
            metadata?.remove(key)
        } else {
            val encoded = codec.encode(value)
            if (encoded.isNull || (encoded is BsonDocument && encoded.isEmpty())) {
                metadata?.remove(key)
            } else {
                metadata?.put(key, encoded)
            }
        }
    }

    /**
     * Sets a raw [BsonValue] metadata entry.
     *
     * @param key The metadata key
     * @param value The BSON value, or null to remove
     */
    public fun metadata(key: String, value: BsonValue?) {
        ensureMetadata()
        if (value == null || value.isNull) {
            metadata?.remove(key)
        } else {
            metadata?.put(key, value)
        }
    }

    /**
     * Configures metadata using a block.
     *
     * Example:
     * ```kotlin
     * metadata {
     *     put("custom_key", BsonString("value"))
     * }
     * ```
     *
     * @param block Configuration block for the BSON document
     */
    public fun metadata(block: BsonDocument.() -> Unit) {
        ensureMetadata()
        metadata?.apply(block)
    }

    private fun ensureMetadata() {
        if (metadata == null) {
            metadata = BsonDocument()
        }
    }

    /**
     * Builds the [ItemStack] instance.
     *
     * @return The constructed item stack
     */
    public fun build(): ItemStack {
        val baseStack = ItemStack(itemId, quantity, metadata)
        val dur = durability
        val maxDur = maxDurability

        return when {
            dur != null && maxDur != null -> ItemStack(itemId, quantity, dur, maxDur, metadata)
            dur != null -> baseStack.withDurability(dur)
            maxDur != null -> baseStack.withMaxDurability(maxDur)
            else -> baseStack
        }
    }
}

/**
 * Creates an [ItemStack] using a DSL builder.
 *
 * Example:
 * ```kotlin
 * val sword = itemStack("sword_iron") {
 *     quantity = 1
 *     durability = 50.0
 *     metadata("enchantments", EnchantmentCodec, myEnchants)
 * }
 * ```
 *
 * @param itemId The item identifier
 * @param block The builder configuration block
 * @return The constructed item stack
 */
public fun itemStack(itemId: String, block: ItemStackBuilder.() -> Unit): ItemStack =
    ItemStackBuilder(itemId).apply(block).build()

/**
 * Creates a simple [ItemStack] with just an item ID.
 *
 * @param itemId The item identifier
 * @return The constructed item stack with quantity 1
 */
public fun itemStack(itemId: String): ItemStack = ItemStack(itemId)

/**
 * Creates a simple [ItemStack] with an item ID and quantity.
 *
 * @param itemId The item identifier
 * @param quantity The quantity of items
 * @return The constructed item stack
 */
public fun itemStack(itemId: String, quantity: Int): ItemStack = ItemStack(itemId, quantity)

/**
 * Modifies an existing [ItemStack] using a DSL builder.
 * Since [ItemStack] is immutable, this creates a new instance with the modifications.
 *
 * Example:
 * ```kotlin
 * val upgradedSword = existingSword.modify {
 *     durability = maxDurability  // Repair the item
 *     metadata("enchantments", EnchantmentCodec, betterEnchants)
 * }
 * ```
 *
 * @param block The modification block
 * @return A new item stack with the modifications applied
 */
public inline fun ItemStack.modify(block: ItemStackModifier.() -> Unit): ItemStack =
    ItemStackModifier(this).apply(block).build()

/**
 * Modifier for existing [ItemStack] instances.
 * Provides a DSL for modifying item stacks immutably.
 *
 * @param source The source item stack to modify
 */
@ItemDslMarker
public class ItemStackModifier(private val source: ItemStack) {
    private var currentStack: ItemStack = source

    /**
     * Gets or sets the quantity.
     * Setting to 0 is not allowed and will throw.
     */
    public var quantity: Int
        get() = currentStack.quantity
        set(value) {
            currentStack = currentStack.withQuantity(value)
                ?: throw IllegalArgumentException("Quantity cannot be 0")
        }

    /**
     * Gets or sets the durability.
     * Clamped to [0, maxDurability].
     */
    public var durability: Double
        get() = currentStack.durability
        set(value) {
            currentStack = currentStack.withDurability(value)
        }

    /**
     * Gets the current max durability.
     */
    public val maxDurability: Double
        get() = currentStack.maxDurability

    /**
     * Sets the maximum durability.
     * Current durability is clamped to not exceed the new max.
     */
    public fun maxDurability(value: Double) {
        currentStack = currentStack.withMaxDurability(value)
    }

    /**
     * Increases durability by the specified amount.
     *
     * @param amount The amount to increase (can be negative to decrease)
     */
    public fun increaseDurability(amount: Double) {
        currentStack = currentStack.withIncreasedDurability(amount)
    }

    /**
     * Restores durability to max.
     *
     * @param newMaxDurability Optional new max durability value
     */
    public fun restoreDurability(newMaxDurability: Double = maxDurability) {
        currentStack = currentStack.withRestoredDurability(newMaxDurability)
    }

    /**
     * Changes the item state.
     *
     * @param state The new state identifier
     */
    public fun state(state: String) {
        currentStack = currentStack.withState(state)
    }

    /**
     * Sets a metadata value using a [KeyedCodec].
     *
     * @param keyedCodec The keyed codec containing both the key and encoder
     * @param value The value to encode, or null to remove
     */
    public fun <T> metadata(keyedCodec: KeyedCodec<T>, value: T?) {
        currentStack = currentStack.withMetadata(keyedCodec, value)
    }

    /**
     * Sets a metadata value using a key and [Codec].
     *
     * @param key The metadata key
     * @param codec The codec to encode the value
     * @param value The value to encode, or null to remove
     */
    public fun <T> metadata(key: String, codec: Codec<T>, value: T?) {
        currentStack = currentStack.withMetadata(key, codec, value)
    }

    /**
     * Sets a raw [BsonValue] metadata entry.
     *
     * @param key The metadata key
     * @param value The BSON value, or null to remove
     */
    public fun metadata(key: String, value: BsonValue?) {
        currentStack = currentStack.withMetadata(key, value)
    }

    /**
     * Replaces all metadata with a new document.
     *
     * @param metadata The new metadata document, or null to clear
     */
    public fun metadata(metadata: BsonDocument?) {
        currentStack = currentStack.withMetadata(metadata)
    }

    /**
     * Builds the modified item stack.
     *
     * @return The new item stack with all modifications
     */
    public fun build(): ItemStack = currentStack
}
