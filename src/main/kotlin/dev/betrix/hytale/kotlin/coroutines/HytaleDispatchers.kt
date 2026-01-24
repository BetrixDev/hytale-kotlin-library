package dev.betrix.hytale.kotlin.coroutines

import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.universe.world.World
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine dispatchers for Hytale server operations.
 *
 * This object provides access to different execution contexts:
 * - [async] - For CPU-bound background work (uses [Dispatchers.Default])
 * - [io] - For I/O-bound operations like database/file access (uses [Dispatchers.IO])
 * - [scheduled] - For scheduled/timed tasks (uses [HytaleServer.SCHEDULED_EXECUTOR])
 *
 * For world-thread execution, use [World.dispatcher] extension property instead.
 *
 * Example:
 * ```kotlin
 * // Run on async thread pool
 * launch(HytaleDispatchers.async) {
 *     val data = computeExpensiveValue()
 *     withContext(world.dispatcher) {
 *         applyToWorld(data)
 *     }
 * }
 *
 * // Run I/O operation
 * launch(HytaleDispatchers.io) {
 *     saveToDatabase(playerData)
 * }
 * ```
 */
public object HytaleDispatchers {
    /**
     * Dispatcher for CPU-bound background work.
     *
     * Uses [Dispatchers.Default] which is backed by a shared pool of threads.
     * Use this for computationally intensive operations that don't block.
     *
     * Example:
     * ```kotlin
     * launch(HytaleDispatchers.async) {
     *     val result = computePathfinding()
     * }
     * ```
     */
    public val async: CoroutineDispatcher
        get() = Dispatchers.Default

    /**
     * Dispatcher for I/O-bound operations.
     *
     * Uses [Dispatchers.IO] which is optimized for blocking I/O operations.
     * Use this for database queries, file operations, and network requests.
     *
     * Example:
     * ```kotlin
     * launch(HytaleDispatchers.io) {
     *     val config = loadConfigFromDisk()
     *     val playerData = queryDatabase(playerId)
     * }
     * ```
     */
    public val io: CoroutineDispatcher
        get() = Dispatchers.IO

    /**
     * Dispatcher backed by Hytale's global scheduled executor.
     *
     * This is a single-threaded executor used by the server for scheduled tasks.
     * Prefer [async] or [io] for most background work. Use this only when you
     * need to integrate with Hytale's native scheduling system.
     *
     * Note: This dispatcher does NOT run on any world thread.
     */
    public val scheduled: CoroutineDispatcher by lazy {
        HytaleServer.SCHEDULED_EXECUTOR.asCoroutineDispatcher()
    }
}

/**
 * Creates a [CoroutineDispatcher] that executes coroutines on this world's thread.
 *
 * All code dispatched to this dispatcher runs on the world's dedicated thread,
 * making it safe to access and modify world state (entities, blocks, chunks).
 *
 * Example:
 * ```kotlin
 * // Execute on world thread
 * launch(world.dispatcher) {
 *     // Safe to modify entities here
 *     entity.teleport(newPosition)
 * }
 *
 * // Switch to world thread from async context
 * launch(HytaleDispatchers.async) {
 *     val data = loadPlayerData()
 *     withContext(world.dispatcher) {
 *         applyData(player, data)
 *     }
 * }
 * ```
 */
public val World.dispatcher: CoroutineDispatcher
    get() = WorldDispatcher(this)

/**
 * A [CoroutineDispatcher] that executes work on a specific [World]'s thread.
 *
 * This dispatcher uses [World.execute] to submit work to the world's task queue,
 * ensuring thread-safe access to world state.
 */
public class WorldDispatcher(
    private val world: World
) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        world.execute(block)
    }

    override fun toString(): String = "WorldDispatcher[${world.name}]"
}
