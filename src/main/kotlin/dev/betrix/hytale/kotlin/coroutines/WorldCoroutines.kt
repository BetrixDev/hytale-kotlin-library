@file:JvmName("WorldCoroutines")

package dev.betrix.hytale.kotlin.coroutines

import com.hypixel.hytale.server.core.universe.world.World
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Launches a coroutine that runs on this world's thread.
 *
 * This is the primary way to run coroutine code that needs to access world state.
 * All code in the coroutine block executes on the world's dedicated thread,
 * making it safe to modify entities, blocks, and other world state.
 *
 * Example:
 * ```kotlin
 * world.launch {
 *     // Runs on world thread - safe to modify entities
 *     player.teleport(spawnPoint)
 *     delay(1000) // Suspends without blocking the world thread
 *     player.sendMessage("Welcome!")
 * }
 * ```
 *
 * @param context Additional coroutine context elements
 * @param start Coroutine start option (default: [CoroutineStart.DEFAULT])
 * @param block The coroutine code to execute
 * @return The launched [Job]
 */
public fun World.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val scope = CoroutineScope(dispatcher + SupervisorJob() + context)
    return scope.launch(start = start, block = block)
}

/**
 * Launches an async coroutine that runs on this world's thread and returns a [Deferred] result.
 *
 * Use this when you need to compute a value on the world thread and await it elsewhere.
 *
 * Example:
 * ```kotlin
 * val playerCount = world.async {
 *     countPlayersInRegion(region)
 * }
 *
 * // Later, await the result
 * val count = playerCount.await()
 * ```
 *
 * @param context Additional coroutine context elements
 * @param start Coroutine start option
 * @param block The coroutine code to execute
 * @return A [Deferred] that will contain the result
 */
public fun <T> World.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val scope = CoroutineScope(dispatcher + SupervisorJob() + context)
    return scope.async(start = start, block = block)
}

/**
 * Switches to this world's thread for the duration of the block.
 *
 * Use this to switch execution context to a world thread from any coroutine.
 * This is the coroutine-equivalent of [World.execute].
 *
 * Example:
 * ```kotlin
 * launch(HytaleDispatchers.io) {
 *     val playerData = loadFromDatabase(playerId)
 *     
 *     world.withWorld {
 *         // Now on world thread - safe to modify entities
 *         player.applyData(playerData)
 *     }
 *     
 *     // Back on IO dispatcher
 *     logSuccess(playerId)
 * }
 * ```
 *
 * @param block The code to execute on the world thread
 * @return The result of the block
 */
public suspend fun <T> World.withWorld(block: suspend CoroutineScope.() -> T): T =
    withContext(dispatcher, block)

/**
 * Executes the given block on the world thread, suspending until complete.
 *
 * This is a suspend-friendly alternative to [World.execute] that allows you to
 * await completion of the world-thread work.
 *
 * Example:
 * ```kotlin
 * // From an async context
 * val position = world.runOnWorld {
 *     player.position.copy()
 * }
 * ```
 *
 * @param block The code to execute on the world thread
 * @return The result of the block
 */
public suspend fun <T> World.runOnWorld(block: () -> T): T =
    withContext(dispatcher) { block() }
