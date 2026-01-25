@file:JvmName("InteractionBuilders")

package com.github.BetrixDev.hytaleKt.interaction

import com.hypixel.hytale.protocol.GameMode
import com.hypixel.hytale.protocol.InteractionSettings
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionCameraSettings
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionEffects
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionRules
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ParallelInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.RepeatInteraction
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SerialInteraction
import java.util.EnumMap

@InteractionDslMarker
public abstract class InteractionBuilder<T : Interaction>(
    private val id: String
) {
    public var viewDistance: Double? = null
    public var horizontalSpeedMultiplier: Float? = null
    public var runTime: Float? = null
    public var cancelOnItemChange: Boolean? = null

    private var effectsValue: InteractionEffects? = null
    private var rulesValue: InteractionRules? = null
    private var cameraValue: InteractionCameraSettings? = null
    private val settings: EnumMap<GameMode, InteractionSettings> = EnumMap(GameMode::class.java)

    public fun effects(block: InteractionEffectsBuilder.() -> Unit) {
        effectsValue = InteractionEffectsBuilder().apply(block).build()
    }

    public fun rules(block: InteractionRulesBuilder.() -> Unit) {
        rulesValue = InteractionRulesBuilder().apply(block).build()
    }

    public fun camera(block: InteractionCameraSettingsBuilder.() -> Unit) {
        cameraValue = InteractionCameraSettingsBuilder().apply(block).build()
    }

    public fun settings(mode: GameMode, block: InteractionSettingsBuilder.() -> Unit) {
        settings[mode] = InteractionSettingsBuilder().apply(block).build()
    }

    public fun settings(mode: GameMode, settings: InteractionSettings) {
        this.settings[mode] = settings
    }

    protected fun applyBaseFields(interaction: Interaction) {
        interaction.setFieldValue("id", id)
        viewDistance?.let { interaction.setFieldValue("viewDistance", it) }
        effectsValue?.let { interaction.setFieldValue("effects", it) }
        horizontalSpeedMultiplier?.let { interaction.setFieldValue("horizontalSpeedMultiplier", it) }
        runTime?.let { interaction.setFieldValue("runTime", it) }
        cancelOnItemChange?.let { interaction.setFieldValue("cancelOnItemChange", it) }
        rulesValue?.let { interaction.setFieldValue("rules", it) }
        if (settings.isNotEmpty()) {
            interaction.setFieldValue("settings", EnumMap(settings))
        }
        cameraValue?.let { interaction.setFieldValue("camera", it) }
    }

    public abstract fun build(): T
}

@InteractionDslMarker
public class SimpleInteractionBuilder internal constructor(
    private val id: String
) : InteractionBuilder<SimpleInteraction>(id) {
    public var next: String? = null
    public var failed: String? = null

    public fun next(interactionId: String) {
        next = interactionId
    }

    public fun failed(interactionId: String) {
        failed = interactionId
    }

    override fun build(): SimpleInteraction {
        val interaction = SimpleInteraction(id)
        applyBaseFields(interaction)
        next?.let { interaction.setFieldValue("next", it) }
        failed?.let { interaction.setFieldValue("failed", it) }
        return interaction
    }
}

@InteractionDslMarker
public class SerialInteractionBuilder internal constructor(
    private val id: String
) : InteractionBuilder<SerialInteraction>(id) {
    private val interactionIds = mutableListOf<String>()

    public fun interactions(vararg ids: String) {
        interactionIds.addAll(ids)
    }

    override fun build(): SerialInteraction {
        val interaction = SerialInteraction()
        applyBaseFields(interaction)
        interaction.setFieldValue("interactions", interactionIds.toTypedArray())
        return interaction
    }
}

@InteractionDslMarker
public class ParallelInteractionBuilder internal constructor(
    private val id: String
) : InteractionBuilder<ParallelInteraction>(id) {
    private val rootIds = mutableListOf<String>()

    public fun roots(vararg ids: String) {
        rootIds.addAll(ids)
    }

    override fun build(): ParallelInteraction {
        val interaction = ParallelInteraction()
        applyBaseFields(interaction)
        interaction.setFieldValue("interactions", rootIds.toTypedArray())
        return interaction
    }
}

@InteractionDslMarker
public class RepeatInteractionBuilder internal constructor(
    private val id: String
) : InteractionBuilder<RepeatInteraction>(id) {
    public var forkRoot: String? = null
    public var repeatCount: Int? = null

    public fun forkRoot(interactionId: String) {
        forkRoot = interactionId
    }

    public fun repeat(times: Int) {
        repeatCount = times
    }

    override fun build(): RepeatInteraction {
        val interaction = RepeatInteraction()
        applyBaseFields(interaction)
        forkRoot?.let { interaction.setFieldValue("forkInteractions", it) }
        repeatCount?.let { interaction.setFieldValue("repeat", it) }
        return interaction
    }
}

@InteractionDslMarker
public class RootInteractionBuilder internal constructor(
    private val id: String
) {
    private val interactionIds = mutableListOf<String>()
    private val settings: EnumMap<GameMode, com.hypixel.hytale.protocol.RootInteractionSettings> =
        EnumMap(GameMode::class.java)
    public var cooldown: com.hypixel.hytale.protocol.InteractionCooldown? = null
    public var requireNewClick: Boolean? = null
    public var clickQueuingTimeout: Float? = null
    private var rulesValue: InteractionRules? = null

    public fun interactions(vararg ids: String) {
        interactionIds.addAll(ids)
    }

    public fun cooldown(block: InteractionCooldownBuilder.() -> Unit) {
        cooldown = InteractionCooldownBuilder().apply(block).build()
    }

    public fun rules(block: InteractionRulesBuilder.() -> Unit) {
        rulesValue = InteractionRulesBuilder().apply(block).build()
    }

    public fun settings(mode: GameMode, block: RootInteractionSettingsBuilder.() -> Unit) {
        settings[mode] = RootInteractionSettingsBuilder().apply(block).build()
    }

    public fun settings(mode: GameMode, settings: com.hypixel.hytale.protocol.RootInteractionSettings) {
        this.settings[mode] = settings
    }

    public fun build(): RootInteraction {
        val root = RootInteraction(id, *interactionIds.toTypedArray())
        cooldown?.let { root.setFieldValue("cooldown", it) }
        if (settings.isNotEmpty()) {
            root.setFieldValue("settings", EnumMap(settings))
        }
        requireNewClick?.let { root.setFieldValue("requireNewClick", it) }
        clickQueuingTimeout?.let { root.setFieldValue("clickQueuingTimeout", it) }
        rulesValue?.let { root.setFieldValue("rules", it) }
        return root
    }
}

public fun simpleInteraction(id: String, block: SimpleInteractionBuilder.() -> Unit): SimpleInteraction =
    SimpleInteractionBuilder(id).apply(block).build()

public fun serialInteraction(id: String, block: SerialInteractionBuilder.() -> Unit): SerialInteraction =
    SerialInteractionBuilder(id).apply(block).build()

public fun parallelInteraction(id: String, block: ParallelInteractionBuilder.() -> Unit): ParallelInteraction =
    ParallelInteractionBuilder(id).apply(block).build()

public fun repeatInteraction(id: String, block: RepeatInteractionBuilder.() -> Unit): RepeatInteraction =
    RepeatInteractionBuilder(id).apply(block).build()

public fun rootInteraction(id: String, block: RootInteractionBuilder.() -> Unit): RootInteraction =
    RootInteractionBuilder(id).apply(block).build()
