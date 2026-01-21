@file:JvmName("ComponentCodecBuilder")

package dev.betrix.hytale.kotlin.ecs.component

import com.hypixel.hytale.codec.Codec
import com.hypixel.hytale.codec.KeyedCodec
import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.component.Component
import java.util.UUID

/**
 * DSL marker for component codec builder to prevent scope leaking.
 */
@DslMarker
public annotation class ComponentCodecDsl

/**
 * Creates a [BuilderCodec] for a component using a type-safe DSL.
 *
 * Example:
 * ```kotlin
 * companion object {
 *     val CODEC = componentCodec(MatchComponent::class.java, ::MatchComponent) {
 *         uuidField("MatchId", MatchComponent::matchId) { c, v -> c.matchId = v }
 *         enumField("State", MatchState::class.java, MatchComponent::state) { c, v -> c.state = v }
 *         intField("Round", MatchComponent::round) { c, v -> c.round = v }
 *     }
 * }
 * ```
 *
 * @param T The component type
 * @param clazz The component class
 * @param defaultFactory Factory function to create default instances
 * @param block The DSL configuration block
 * @return A configured BuilderCodec
 */
public inline fun <T : Component<*>> componentCodec(
    clazz: Class<T>,
    noinline defaultFactory: () -> T,
    block: ComponentCodecScope<T>.() -> Unit
): BuilderCodec<T> = ComponentCodecScope(clazz, defaultFactory).apply(block).build()

/**
 * Creates a [BuilderCodec] using reified generics.
 *
 * @param T The component type
 * @param defaultFactory Factory function to create default instances
 * @param block The DSL configuration block
 * @return A configured BuilderCodec
 */
public inline fun <reified T : Component<*>> componentCodec(
    noinline defaultFactory: () -> T,
    block: ComponentCodecScope<T>.() -> Unit
): BuilderCodec<T> = componentCodec(T::class.java, defaultFactory, block)

/**
 * Scope class for building component codecs with type-safe field registration.
 */
@ComponentCodecDsl
public class ComponentCodecScope<T : Component<*>>(
    private val clazz: Class<T>,
    private val defaultFactory: () -> T
) {
    @PublishedApi
    internal var builder: BuilderCodec.Builder<T> = BuilderCodec.builder(clazz, defaultFactory)

    /**
     * Adds a String field to the codec.
     */
    public fun stringField(
        name: String,
        getter: (T) -> String,
        setter: (T, String) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.STRING), setter, getter).add()
    }

    /**
     * Adds an Int field to the codec.
     */
    public fun intField(
        name: String,
        getter: (T) -> Int,
        setter: (T, Int) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.INTEGER), setter, getter).add()
    }

    /**
     * Adds a Long field to the codec.
     */
    public fun longField(
        name: String,
        getter: (T) -> Long,
        setter: (T, Long) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.LONG), setter, getter).add()
    }

    /**
     * Adds a Float field to the codec.
     */
    public fun floatField(
        name: String,
        getter: (T) -> Float,
        setter: (T, Float) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.FLOAT), setter, getter).add()
    }

    /**
     * Adds a Double field to the codec.
     */
    public fun doubleField(
        name: String,
        getter: (T) -> Double,
        setter: (T, Double) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.DOUBLE), setter, getter).add()
    }

    /**
     * Adds a Boolean field to the codec.
     */
    public fun boolField(
        name: String,
        getter: (T) -> Boolean,
        setter: (T, Boolean) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, Codec.BOOLEAN), setter, getter).add()
    }

    /**
     * Adds a UUID field to the codec.
     * UUIDs are serialized as strings.
     */
    public fun uuidField(
        name: String,
        getter: (T) -> UUID,
        setter: (T, UUID) -> Unit
    ) {
        builder = builder.append(
            KeyedCodec(name, Codec.STRING),
            { c, v -> setter(c, UUID.fromString(v)) },
            { c -> getter(c).toString() }
        ).add()
    }

    /**
     * Adds a nullable UUID field to the codec.
     * UUIDs are serialized as strings.
     */
    public fun nullableUuidField(
        name: String,
        getter: (T) -> UUID?,
        setter: (T, UUID?) -> Unit
    ) {
        builder = builder.append(
            KeyedCodec(name, Codec.STRING),
            { c, v -> setter(c, v?.takeIf { it.isNotEmpty() }?.let { UUID.fromString(it) }) },
            { c -> getter(c)?.toString() ?: "" }
        ).add()
    }

    /**
     * Adds an Enum field to the codec.
     * Enums are serialized as strings (their name).
     */
    public fun <E : Enum<E>> enumField(
        name: String,
        enumClass: Class<E>,
        getter: (T) -> E,
        setter: (T, E) -> Unit
    ) {
        builder = builder.append(
            KeyedCodec(name, Codec.STRING),
            { c, v -> setter(c, java.lang.Enum.valueOf(enumClass, v)) },
            { c -> getter(c).name }
        ).add()
    }

    /**
     * Adds a nullable Enum field to the codec.
     */
    public fun <E : Enum<E>> nullableEnumField(
        name: String,
        enumClass: Class<E>,
        getter: (T) -> E?,
        setter: (T, E?) -> Unit
    ) {
        builder = builder.append(
            KeyedCodec(name, Codec.STRING),
            { c, v -> setter(c, v?.takeIf { it.isNotEmpty() }?.let { java.lang.Enum.valueOf(enumClass, it) }) },
            { c -> getter(c)?.name ?: "" }
        ).add()
    }

    /**
     * Adds a field using a custom codec.
     */
    public fun <F> customField(
        name: String,
        codec: Codec<F>,
        getter: (T) -> F,
        setter: (T, F) -> Unit
    ) {
        builder = builder.append(KeyedCodec(name, codec), setter, getter).add()
    }

    /**
     * Builds the final codec.
     */
    @PublishedApi
    internal fun build(): BuilderCodec<T> = builder.build()
}
