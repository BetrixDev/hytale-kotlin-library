@file:JvmName("TickDelay")

package com.github.BetrixDev.hytaleKt.coroutines

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Default ticks per second for Hytale servers.
 */
public const val DEFAULT_TPS: Int = 30

/**
 * Duration of one tick at default TPS (approximately 33.33ms).
 */
public val TICK_DURATION: Duration = (1000.0 / DEFAULT_TPS).milliseconds

/**
 * Converts this number of ticks to a [Duration].
 *
 * Uses the default server TPS of 30, meaning each tick is approximately 33.33ms.
 *
 * Example:
 * ```kotlin
 * delay(10.ticks) // Delays for ~333ms (10 ticks at 30 TPS)
 * delay(30.ticks) // Delays for ~1 second
 * ```
 */
public val Int.ticks: Duration
    get() = (this * 1000.0 / DEFAULT_TPS).milliseconds

/**
 * Converts this number of ticks to a [Duration].
 *
 * Example:
 * ```kotlin
 * delay(1.5.ticks) // Delays for ~50ms (1.5 ticks)
 * ```
 */
public val Double.ticks: Duration
    get() = (this * 1000.0 / DEFAULT_TPS).milliseconds

/**
 * Converts this number of ticks to a [Duration].
 */
public val Long.ticks: Duration
    get() = (this * 1000.0 / DEFAULT_TPS).milliseconds

/**
 * Suspends the coroutine for the specified number of ticks.
 *
 * This is a convenience function for `delay(ticks.ticks)`.
 *
 * Example:
 * ```kotlin
 * world.launch {
 *     player.sendMessage("Get ready...")
 *     delayTicks(30) // Wait 1 second
 *     player.sendMessage("Go!")
 * }
 * ```
 *
 * @param ticks Number of ticks to delay
 */
public suspend fun delayTicks(ticks: Int) {
    delay(ticks.ticks)
}

/**
 * Suspends the coroutine for the specified number of ticks.
 *
 * @param ticks Number of ticks to delay
 */
public suspend fun delayTicks(ticks: Long) {
    delay(ticks.ticks)
}

/**
 * Executes an action every tick for the specified number of ticks.
 *
 * The action receives the current tick index (0-based).
 *
 * Example:
 * ```kotlin
 * world.launch {
 *     // Countdown from 3 to 1
 *     repeatTicks(90) { tick -> // 3 seconds at 30 TPS
 *         if (tick % 30 == 0) {
 *             val secondsLeft = 3 - (tick / 30)
 *             player.sendMessage("$secondsLeft...")
 *         }
 *     }
 *     player.sendMessage("Go!")
 * }
 * ```
 *
 * @param ticks Total number of ticks to run
 * @param action The action to execute each tick, receives tick index
 */
public suspend fun repeatTicks(ticks: Int, action: suspend (tick: Int) -> Unit) {
    repeat(ticks) { tick ->
        action(tick)
        delay(TICK_DURATION)
    }
}

/**
 * Executes an action repeatedly until cancelled, with a delay between iterations.
 *
 * Example:
 * ```kotlin
 * val job = world.launch {
 *     repeatEvery(5.ticks) {
 *         updateScoreboard()
 *     }
 * }
 *
 * // Later, cancel the repeating task
 * job.cancel()
 * ```
 *
 * @param interval Duration between action executions
 * @param action The action to execute repeatedly
 */
public suspend fun repeatEvery(interval: Duration, action: suspend () -> Unit) {
    while (true) {
        action()
        delay(interval)
    }
}

/**
 * Executes an action repeatedly until cancelled, with a delay between iterations.
 *
 * Convenience overload that accepts ticks as an Int.
 *
 * Example:
 * ```kotlin
 * repeatEveryTicks(30) { // Every second
 *     broadcastPlayerCount()
 * }
 * ```
 *
 * @param ticks Number of ticks between action executions
 * @param action The action to execute repeatedly
 */
public suspend fun repeatEveryTicks(ticks: Int, action: suspend () -> Unit) {
    repeatEvery(ticks.ticks, action)
}
