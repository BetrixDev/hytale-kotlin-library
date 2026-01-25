@file:JvmName("InteractionDsl")

package com.github.BetrixDev.hytaleKt.interaction

import java.lang.reflect.Field

/**
 * DSL marker for interaction builders to prevent nested DSL scope leaking.
 */
@DslMarker
public annotation class InteractionDslMarker

internal fun Any.setFieldValue(fieldName: String, value: Any?) {
    val field = findField(fieldName)
    field.trySetAccessible()
    field.set(this, value)
}

private fun Any.findField(fieldName: String): Field {
    var current: Class<*>? = javaClass
    while (current != null) {
        try {
            return current.getDeclaredField(fieldName)
        } catch (_: NoSuchFieldException) {
            current = current.superclass
        }
    }
    throw IllegalArgumentException("Field '$fieldName' not found on ${javaClass.name}")
}
