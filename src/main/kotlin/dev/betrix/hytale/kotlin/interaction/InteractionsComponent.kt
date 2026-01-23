@file:JvmName("InteractionComponents")

package dev.betrix.hytale.kotlin.interaction

import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.modules.interaction.Interactions

@InteractionDslMarker
public class InteractionsBuilder {
    private val interactions: Interactions = Interactions()
    public var hint: String? = null

    public fun type(type: InteractionType, interactionId: String) {
        interactions.setInteractionId(type, interactionId)
    }

    public fun build(): Interactions {
        hint?.let { interactions.setInteractionHint(it) }
        return interactions
    }
}

public fun interactions(block: InteractionsBuilder.() -> Unit): Interactions =
    InteractionsBuilder().apply(block).build()
