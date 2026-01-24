@file:JvmName("PluginCoroutines")

package dev.betrix.hytale.kotlin.coroutines

import com.hypixel.hytale.server.core.plugin.JavaPlugin
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Storage for plugin coroutine scopes, keyed by plugin class name.
 */
private val pluginScopes = ConcurrentHashMap<String, CoroutineScope>()

/**
 * Gets or creates a [CoroutineScope] tied to this plugin's lifecycle.
 *
 * The scope is automatically cancelled when the plugin is disabled.
 * All coroutines launched from this scope use [Dispatchers.Default] by default.
 *
 * Example:
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     override fun onLoad() {
 *         // Launch a coroutine tied to plugin lifecycle
 *         pluginScope.launch {
 *             while (isActive) {
 *                 performPeriodicTask()
 *                 delay(5000)
 *             }
 *         }
 *     }
 * }
 * ```
 */
public val JavaPlugin.pluginScope: CoroutineScope
    get() = pluginScopes.getOrPut(this::class.java.name) {
        CoroutineScope(
            SupervisorJob() +
            Dispatchers.Default +
            CoroutineName("Plugin-${this::class.java.simpleName}")
        )
    }

/**
 * Cancels this plugin's coroutine scope.
 *
 * Call this in your plugin's shutdown/disable method to cancel all running coroutines.
 *
 * Example:
 * ```kotlin
 * override fun onDisable() {
 *     cancelPluginScope()
 * }
 * ```
 *
 * @param message Optional cancellation message
 */
public fun JavaPlugin.cancelPluginScope(message: String = "Plugin disabled") {
    pluginScopes.remove(this::class.java.name)?.cancel(CancellationException(message))
}

/**
 * Launches a coroutine in the plugin's scope.
 *
 * This is a convenience function equivalent to `pluginScope.launch { ... }`.
 * The coroutine will be automatically cancelled when [cancelPluginScope] is called.
 *
 * Example:
 * ```kotlin
 * override fun onLoad() {
 *     launch {
 *         // Runs on Dispatchers.Default
 *         val result = computeSomething()
 *         world.withWorld {
 *             applyResult(result)
 *         }
 *     }
 * }
 * ```
 *
 * @param context Additional coroutine context elements
 * @param start Coroutine start option
 * @param block The coroutine code to execute
 * @return The launched [Job]
 */
public fun JavaPlugin.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = pluginScope.launch(context, start, block)

/**
 * Launches an async coroutine in the plugin's scope and returns a [Deferred] result.
 *
 * Example:
 * ```kotlin
 * val config = async(HytaleDispatchers.io) {
 *     loadConfigFromDisk()
 * }
 *
 * // Later
 * val loadedConfig = config.await()
 * ```
 *
 * @param context Additional coroutine context elements
 * @param start Coroutine start option
 * @param block The coroutine code to execute
 * @return A [Deferred] containing the result
 */
public fun <T> JavaPlugin.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> = pluginScope.async(context, start, block)

/**
 * Launches a coroutine on an async thread pool (background work).
 *
 * This is equivalent to `launch(HytaleDispatchers.async) { ... }`.
 * Use this for CPU-intensive background computations.
 *
 * Example:
 * ```kotlin
 * launchAsync {
 *     val result = expensiveComputation()
 *     world.withWorld {
 *         applyResult(result)
 *     }
 * }
 * ```
 *
 * @param context Additional coroutine context elements
 * @param block The coroutine code to execute
 * @return The launched [Job]
 */
public fun JavaPlugin.launchAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = pluginScope.launch(HytaleDispatchers.async + context, block = block)

/**
 * Launches a coroutine for I/O operations.
 *
 * This is equivalent to `launch(HytaleDispatchers.io) { ... }`.
 * Use this for database queries, file I/O, and network requests.
 *
 * Example:
 * ```kotlin
 * launchIO {
 *     val data = queryDatabase(playerId)
 *     world.withWorld {
 *         player.applyData(data)
 *     }
 * }
 * ```
 *
 * @param context Additional coroutine context elements
 * @param block The coroutine code to execute
 * @return The launched [Job]
 */
public fun JavaPlugin.launchIO(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = pluginScope.launch(HytaleDispatchers.io + context, block = block)
