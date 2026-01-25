@file:JvmName("InteractionSupportBuilders")

package com.github.BetrixDev.hytaleKt.interaction

import com.hypixel.hytale.protocol.Direction
import com.hypixel.hytale.protocol.InteractionCooldown
import com.hypixel.hytale.protocol.InteractionSettings
import com.hypixel.hytale.protocol.InteractionType
import com.hypixel.hytale.protocol.RootInteractionSettings
import com.hypixel.hytale.protocol.Vector3f
import com.hypixel.hytale.server.core.asset.modifiers.MovementEffects
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionCameraSettings
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionEffects
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.InteractionRules
import java.util.EnumSet

@InteractionDslMarker
public class InteractionCooldownBuilder {
    public var id: String? = null
    public var cooldown: Float = 0.0f
    public var clickBypass: Boolean = false
    public var chargeTimes: FloatArray? = null
    public var skipCooldownReset: Boolean = false
    public var interruptRecharge: Boolean = false

    public fun build(): InteractionCooldown = InteractionCooldown(
        id,
        cooldown,
        clickBypass,
        chargeTimes,
        skipCooldownReset,
        interruptRecharge
    )
}

@InteractionDslMarker
public class InteractionSettingsBuilder {
    public var allowSkipOnClick: Boolean = false

    public fun build(): InteractionSettings = InteractionSettings(allowSkipOnClick)
}

@InteractionDslMarker
public class RootInteractionSettingsBuilder {
    public var allowSkipChainOnClick: Boolean = false
    public var cooldown: InteractionCooldown? = null

    public fun cooldown(block: InteractionCooldownBuilder.() -> Unit) {
        cooldown = InteractionCooldownBuilder().apply(block).build()
    }

    public fun build(): RootInteractionSettings = RootInteractionSettings(allowSkipChainOnClick, cooldown)
}

@InteractionDslMarker
public class InteractionRulesBuilder {
    private var blockedByTypes: EnumSet<InteractionType>? = null
    private var blockingTypes: EnumSet<InteractionType>? = null
    private var interruptedByTypes: EnumSet<InteractionType>? = null
    private var interruptingTypes: EnumSet<InteractionType>? = null

    public var blockedByBypassTag: String? = null
    public var blockingBypassTag: String? = null
    public var interruptedByBypassTag: String? = null
    public var interruptingBypassTag: String? = null

    public fun blockedBy(vararg types: InteractionType) {
        blockedByTypes = if (types.isEmpty()) {
            EnumSet.noneOf(InteractionType::class.java)
        } else {
            EnumSet.copyOf(types.asList())
        }
    }

    public fun blocking(vararg types: InteractionType) {
        blockingTypes = if (types.isEmpty()) {
            EnumSet.noneOf(InteractionType::class.java)
        } else {
            EnumSet.copyOf(types.asList())
        }
    }

    public fun interruptedBy(vararg types: InteractionType) {
        interruptedByTypes = if (types.isEmpty()) {
            EnumSet.noneOf(InteractionType::class.java)
        } else {
            EnumSet.copyOf(types.asList())
        }
    }

    public fun interrupting(vararg types: InteractionType) {
        interruptingTypes = if (types.isEmpty()) {
            EnumSet.noneOf(InteractionType::class.java)
        } else {
            EnumSet.copyOf(types.asList())
        }
    }

    public fun build(): InteractionRules {
        val rules = InteractionRules()
        blockedByTypes?.let { rules.setFieldValue("blockedBy", it) }
        blockingTypes?.let { rules.setFieldValue("blocking", it) }
        interruptedByTypes?.let { rules.setFieldValue("interruptedBy", it) }
        interruptingTypes?.let { rules.setFieldValue("interrupting", it) }
        blockedByBypassTag?.let { rules.setFieldValue("blockedByBypass", it) }
        blockingBypassTag?.let { rules.setFieldValue("blockingBypass", it) }
        interruptedByBypassTag?.let { rules.setFieldValue("interruptedByBypass", it) }
        interruptingBypassTag?.let { rules.setFieldValue("interruptingBypass", it) }
        return rules
    }
}

@InteractionDslMarker
public class InteractionEffectsBuilder {
    public var particles: Array<ModelParticle>? = null
    public var firstPersonParticles: Array<ModelParticle>? = null
    public var worldSoundEventId: String? = null
    public var localSoundEventId: String? = null
    public var trails: Array<com.hypixel.hytale.protocol.ModelTrail>? = null
    public var waitForAnimationToFinish: Boolean? = null
    public var itemPlayerAnimationsId: String? = null
    public var itemAnimationId: String? = null
    public var clearAnimationOnFinish: Boolean? = null
    public var clearSoundEventOnFinish: Boolean? = null
    public var cameraEffectId: String? = null
    public var movementEffects: MovementEffects? = null
    public var startDelay: Float? = null

public fun particles(vararg values: ModelParticle) {
        particles = values.asList().toTypedArray()
    }

    public fun firstPersonParticles(vararg values: ModelParticle) {
        firstPersonParticles = values.asList().toTypedArray()
    }

    public fun trails(vararg values: com.hypixel.hytale.protocol.ModelTrail) {
        trails = values.asList().toTypedArray()
    }

    public fun build(): InteractionEffects {
        val effects = InteractionEffects::class.java.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        particles?.let { effects.setFieldValue("particles", it) }
        firstPersonParticles?.let { effects.setFieldValue("firstPersonParticles", it) }
        worldSoundEventId?.let { effects.setFieldValue("worldSoundEventId", it) }
        localSoundEventId?.let { effects.setFieldValue("localSoundEventId", it) }
        trails?.let { effects.setFieldValue("trails", it) }
        waitForAnimationToFinish?.let { effects.setFieldValue("waitForAnimationToFinish", it) }
        itemPlayerAnimationsId?.let { effects.setFieldValue("itemPlayerAnimationsId", it) }
        itemAnimationId?.let { effects.setFieldValue("itemAnimationId", it) }
        clearAnimationOnFinish?.let { effects.setFieldValue("clearAnimationOnFinish", it) }
        clearSoundEventOnFinish?.let { effects.setFieldValue("clearSoundEventOnFinish", it) }
        cameraEffectId?.let { effects.setFieldValue("cameraEffectId", it) }
        movementEffects?.let { effects.setFieldValue("movementEffects", it) }
        startDelay?.let { effects.setFieldValue("startDelay", it) }
        return effects
    }
}

@InteractionDslMarker
public class InteractionCameraSettingsBuilder {
    private val firstPerson = mutableListOf<InteractionCameraSettings.InteractionCamera>()
    private val thirdPerson = mutableListOf<InteractionCameraSettings.InteractionCamera>()

    public fun firstPerson(block: InteractionCameraListBuilder.() -> Unit) {
        firstPerson.addAll(InteractionCameraListBuilder().apply(block).build())
    }

    public fun thirdPerson(block: InteractionCameraListBuilder.() -> Unit) {
        thirdPerson.addAll(InteractionCameraListBuilder().apply(block).build())
    }

    public fun build(): InteractionCameraSettings {
        val settings = InteractionCameraSettings()
        if (firstPerson.isNotEmpty()) {
            settings.setFieldValue("firstPerson", firstPerson.toTypedArray())
        }
        if (thirdPerson.isNotEmpty()) {
            settings.setFieldValue("thirdPerson", thirdPerson.toTypedArray())
        }
        return settings
    }
}

@InteractionDslMarker
public class InteractionCameraListBuilder {
    private val entries = mutableListOf<InteractionCameraSettings.InteractionCamera>()

    public fun camera(time: Float, position: Vector3f, rotation: Direction) {
        val entry = InteractionCameraSettings.InteractionCamera()
        entry.setFieldValue("time", time)
        entry.setFieldValue("position", position)
        entry.setFieldValue("rotation", rotation)
        entries.add(entry)
    }

    public fun build(): List<InteractionCameraSettings.InteractionCamera> = entries
}

public fun interactionCooldown(block: InteractionCooldownBuilder.() -> Unit): InteractionCooldown =
    InteractionCooldownBuilder().apply(block).build()

public fun interactionSettings(block: InteractionSettingsBuilder.() -> Unit): InteractionSettings =
    InteractionSettingsBuilder().apply(block).build()

public fun rootInteractionSettings(block: RootInteractionSettingsBuilder.() -> Unit): RootInteractionSettings =
    RootInteractionSettingsBuilder().apply(block).build()

public fun interactionRules(block: InteractionRulesBuilder.() -> Unit): InteractionRules =
    InteractionRulesBuilder().apply(block).build()

public fun interactionEffects(block: InteractionEffectsBuilder.() -> Unit): InteractionEffects =
    InteractionEffectsBuilder().apply(block).build()

public fun interactionCameraSettings(block: InteractionCameraSettingsBuilder.() -> Unit): InteractionCameraSettings =
    InteractionCameraSettingsBuilder().apply(block).build()
