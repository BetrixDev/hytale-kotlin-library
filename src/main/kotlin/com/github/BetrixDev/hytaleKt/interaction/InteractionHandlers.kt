@file:JvmName("InteractionHandlers")
@file:Suppress("DEPRECATION")

package com.github.BetrixDev.hytaleKt.interaction

import com.hypixel.hytale.component.Ref
import com.hypixel.hytale.component.Store
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.server.core.entity.InteractionChain
import com.hypixel.hytale.server.core.entity.InteractionContext
import com.hypixel.hytale.server.core.entity.InteractionManager
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent
import com.hypixel.hytale.server.core.inventory.ItemStack
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import com.github.BetrixDev.hytaleKt.events.registerEvent
import com.hypixel.hytale.math.vector.Vector3i
import com.hypixel.hytale.server.core.entity.Entity
import java.util.EnumSet

@InteractionDslMarker
public class InteractionHandlerScope {
    private val handlers = mutableListOf<InteractionHandler>()

    public fun on(vararg types: InteractionType, block: InteractionHandlerBuilder.() -> Unit) {
        require(types.isNotEmpty()) { "Interaction handler requires at least one type." }
        val typeSet = EnumSet.copyOf(types.asList())
        handlers.add(InteractionHandlerBuilder(typeSet).apply(block).build())
    }

    public fun onAny(block: InteractionHandlerBuilder.() -> Unit) {
        handlers.add(InteractionHandlerBuilder(null).apply(block).build())
    }

    internal fun build(): List<InteractionHandler> = handlers.toList()
}

@InteractionDslMarker
public class InteractionHandlerBuilder internal constructor(
    private val types: EnumSet<InteractionType>?
) {
    private var predicate: InteractionHandlerContext.() -> Boolean = { true }
    private var handler: InteractionHandlerContext.() -> Unit = {}

    public fun filter(block: InteractionHandlerContext.() -> Boolean) {
        predicate = block
    }

    public fun handle(block: InteractionHandlerContext.() -> Unit) {
        handler = block
    }

    internal fun build(): InteractionHandler = InteractionHandler(types, predicate, handler)
}

public class InteractionHandlerContext internal constructor(
    public val event: PlayerInteractEvent
) {
    private var propagationStopped = false

    public val type: InteractionType = event.actionType
    public val player: Player = event.player
    public val playerRef: Ref<EntityStore> = event.playerRef
    public val itemInHand: ItemStack? = event.itemInHand
    public val targetBlock: Vector3i? = event.targetBlock
    public val targetEntity: Entity? = event.targetEntity
    public val targetRef: Ref<EntityStore>? = event.targetRef
    public val store: Store<EntityStore> = playerRef.store

    public fun cancel() {
        event.isCancelled = true
    }

    public fun stopPropagation() {
        propagationStopped = true
    }

    public fun interactionContext(equipSlot: Int? = null): InteractionContext? {
        val manager = interactionManager() ?: return null
        if (type == InteractionType.Equipped) {
            requireNotNull(equipSlot) { "InteractionType.Equipped requires an equipSlot." }
            return InteractionContext.forInteraction(manager, playerRef, type, equipSlot, store)
        }
        return InteractionContext.forInteraction(manager, playerRef, type, store)
    }

    public fun startRoot(rootId: String, equipSlot: Int? = null, forceRemoteSync: Boolean = false): InteractionChain? {
        val manager = interactionManager() ?: return null
        val context = interactionContext(equipSlot) ?: return null
        val root = RootInteraction.getAssetMap().getAsset(rootId) ?: return null
        val chain = manager.initChain(type, context, root, forceRemoteSync)
        manager.queueExecuteChain(chain)
        return chain
    }

    public fun startDefaultRoot(equipSlot: Int? = null, forceRemoteSync: Boolean = false): InteractionChain? {
        val manager = interactionManager() ?: return null
        val context = interactionContext(equipSlot) ?: return null
        val rootId = context.getRootInteractionId(type) ?: return null
        val root = RootInteraction.getAssetMap().getAsset(rootId) ?: return null
        val chain = manager.initChain(type, context, root, forceRemoteSync)
        manager.queueExecuteChain(chain)
        return chain
    }

    internal fun isPropagationStopped(): Boolean = propagationStopped

    private fun interactionManager(): InteractionManager? {
        return store.getComponent(playerRef, InteractionModule.get().getInteractionManagerComponent())
    }
}

internal data class InteractionHandler(
    val types: EnumSet<InteractionType>?,
    val predicate: InteractionHandlerContext.() -> Boolean,
    val handler: InteractionHandlerContext.() -> Unit
)

@Suppress("UNCHECKED_CAST")
public fun JavaPlugin.registerInteractionHandlers(
    block: InteractionHandlerScope.() -> Unit
): com.hypixel.hytale.event.EventRegistration<*, *> {
    val handlers = InteractionHandlerScope().apply(block).build()
    return eventRegistry.register(PlayerInteractEvent::class.java) { event ->
        val context = InteractionHandlerContext(event as PlayerInteractEvent)
        for (handler in handlers) {
            val matches = handler.types?.contains(context.type) ?: true
            if (!matches || !handler.predicate(context)) {
                continue
            }
            handler.handler(context)
            if (context.isPropagationStopped()) {
                break
            }
        }
    }!!
}
