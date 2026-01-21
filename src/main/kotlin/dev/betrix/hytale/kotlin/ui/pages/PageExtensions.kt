@file:JvmName("PageExtensions")

package dev.betrix.hytale.kotlin.ui.pages

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.ui.builder.EventData
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

/**
 * Binds an Activating event to a UI element with data pairs.
 * This is a shorthand for the verbose addEventBinding call.
 *
 * Example:
 * ```kotlin
 * eventBuilder.bindActivating("#SkeletonButton", "Type" to "SelectKit", "KitId" to "skeleton")
 * eventBuilder.bindActivating("#ClearKit", "Type" to "ClearKit")
 * ```
 *
 * @param selector The UI element selector
 * @param data The data pairs to include in the event
 * @return This builder for chaining
 */
public fun UIEventBuilder.bindActivating(
    selector: String,
    vararg data: Pair<String, Any>
): UIEventBuilder {
    val eventData = EventData()
    data.forEach { (key, value) ->
        eventData.append(key, value.toString())
    }
    return addEventBinding(CustomUIEventBindingType.Activating, selector, eventData, false)
}

/**
 * Binds an Activating event with interface locking.
 *
 * @param selector The UI element selector
 * @param locksInterface Whether the event should lock the interface
 * @param data The data pairs to include in the event
 * @return This builder for chaining
 */
public fun UIEventBuilder.bindActivating(
    selector: String,
    locksInterface: Boolean,
    vararg data: Pair<String, Any>
): UIEventBuilder {
    val eventData = EventData()
    data.forEach { (key, value) ->
        eventData.append(key, value.toString())
    }
    return addEventBinding(CustomUIEventBindingType.Activating, selector, eventData, locksInterface)
}

/**
 * Binds a ValueChanged event to a UI element.
 *
 * Example:
 * ```kotlin
 * eventBuilder.bindValueChanged("#SearchInput", "@SearchQuery" to "#SearchInput.Value")
 * ```
 *
 * @param selector The UI element selector
 * @param data The data pairs to include in the event
 * @return This builder for chaining
 */
public fun UIEventBuilder.bindValueChanged(
    selector: String,
    vararg data: Pair<String, Any>
): UIEventBuilder {
    val eventData = EventData()
    data.forEach { (key, value) ->
        eventData.append(key, value.toString())
    }
    return addEventBinding(CustomUIEventBindingType.ValueChanged, selector, eventData, false)
}

/**
 * Binds a MouseEntered event to a UI element.
 *
 * @param selector The UI element selector
 * @param data The data pairs to include in the event
 * @return This builder for chaining
 */
public fun UIEventBuilder.bindMouseEntered(
    selector: String,
    vararg data: Pair<String, Any>
): UIEventBuilder {
    val eventData = EventData()
    data.forEach { (key, value) ->
        eventData.append(key, value.toString())
    }
    return addEventBinding(CustomUIEventBindingType.MouseEntered, selector, eventData, false)
}

/**
 * Binds a MouseExited event to a UI element.
 *
 * @param selector The UI element selector
 * @param data The data pairs to include in the event
 * @return This builder for chaining
 */
public fun UIEventBuilder.bindMouseExited(
    selector: String,
    vararg data: Pair<String, Any>
): UIEventBuilder {
    val eventData = EventData()
    data.forEach { (key, value) ->
        eventData.append(key, value.toString())
    }
    return addEventBinding(CustomUIEventBindingType.MouseExited, selector, eventData, false)
}

/**
 * Gets the Player component from the store for the given reference.
 * This is a common operation in page handlers.
 *
 * Example:
 * ```kotlin
 * val player = store.getPlayer(ref) ?: return sendUpdate()
 * ```
 *
 * @param ref The entity reference
 * @return The Player component or null
 */
public fun Store<EntityStore>.getPlayer(ref: Ref<EntityStore>): Player? =
    getComponent(ref, Player.getComponentType())

/**
 * Gets the Player component from the store, throwing if not found.
 *
 * @param ref The entity reference
 * @return The Player component
 * @throws IllegalStateException if the player component is not found
 */
public fun Store<EntityStore>.requirePlayer(ref: Ref<EntityStore>): Player =
    getPlayer(ref) ?: throw IllegalStateException("Player component not found")

/**
 * Interface for typed page data that includes an action type.
 * Implementing this interface allows using when expressions for type-safe action handling.
 *
 * Example:
 * ```kotlin
 * class PageData : TypedPageData {
 *     override var type: String = ""
 *     var kitId: String? = null
 * }
 *
 * // In handleDataEvent:
 * when (data.type) {
 *     "SelectKit" -> handleSelectKit(data.kitId)
 *     "ClearKit" -> handleClearKit()
 * }
 * ```
 */
public interface TypedPageData {
    /**
     * The action type identifier for this page data.
     */
    public val type: String
}
