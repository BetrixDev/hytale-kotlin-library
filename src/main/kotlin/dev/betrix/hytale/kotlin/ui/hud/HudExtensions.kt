@file:JvmName("HudExtensions")

package dev.betrix.hytale.kotlin.ui.hud

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder

/**
 * Sets a text value on a UI element using TextSpans.
 *
 * Example:
 * ```kotlin
 * builder.setText("#TimeValue", "5:00")
 * builder.setText("#PlayerName", playerName)
 * ```
 *
 * @param selector The UI element selector (e.g., "#TimeValue")
 * @param text The text to display
 * @return This builder for chaining
 */
public fun UICommandBuilder.setText(selector: String, text: String): UICommandBuilder =
    set("$selector.TextSpans", Message.raw(text))

/**
 * Sets a formatted text value on a UI element using TextSpans.
 *
 * Example:
 * ```kotlin
 * builder.setFormattedText("#Score", "Score: %d", score)
 * builder.setFormattedText("#Timer", "%d:%02d", minutes, seconds)
 * ```
 *
 * @param selector The UI element selector
 * @param format The format string
 * @param args The format arguments
 * @return This builder for chaining
 */
public fun UICommandBuilder.setFormattedText(
    selector: String,
    format: String,
    vararg args: Any
): UICommandBuilder = set("$selector.TextSpans", Message.raw(String.format(format, *args)))

/**
 * Sets a translatable message on a UI element.
 *
 * Example:
 * ```kotlin
 * builder.setTranslation("#WelcomeText", "ui.welcome.message")
 * ```
 *
 * @param selector The UI element selector
 * @param translationKey The translation key
 * @return This builder for chaining
 */
public fun UICommandBuilder.setTranslation(
    selector: String,
    translationKey: String
): UICommandBuilder = set("$selector.TextSpans", Message.translation(translationKey))

/**
 * Sets visibility of a UI element.
 *
 * Example:
 * ```kotlin
 * builder.setVisible("#LoadingSpinner", isLoading)
 * ```
 *
 * @param selector The UI element selector
 * @param visible Whether the element should be visible
 * @return This builder for chaining
 */
public fun UICommandBuilder.setVisible(selector: String, visible: Boolean): UICommandBuilder =
    set("$selector.Visible", visible)

/**
 * Sets the enabled state of a UI element.
 *
 * Example:
 * ```kotlin
 * builder.setEnabled("#SubmitButton", canSubmit)
 * ```
 *
 * @param selector The UI element selector
 * @param enabled Whether the element should be enabled
 * @return This builder for chaining
 */
public fun UICommandBuilder.setEnabled(selector: String, enabled: Boolean): UICommandBuilder =
    set("$selector.Enabled", enabled)

/**
 * Sends a partial update to the HUD without rebuilding it entirely.
 * This is more efficient than calling show() for small updates.
 *
 * Example:
 * ```kotlin
 * sendPartialUpdate {
 *     setText("#TimeValue", formatTime(remainingSeconds))
 *     setText("#Score", score.toString())
 * }
 * ```
 *
 * @param block The builder block for the update commands
 */
public inline fun CustomUIHud.sendPartialUpdate(block: UICommandBuilder.() -> Unit) {
    val builder = UICommandBuilder()
    builder.block()
    update(false, builder)
}

/**
 * Sends a partial update that clears previous content first.
 *
 * @param block The builder block for the update commands
 */
public inline fun CustomUIHud.sendClearingUpdate(block: UICommandBuilder.() -> Unit) {
    val builder = UICommandBuilder()
    builder.block()
    update(true, builder)
}

/**
 * Formats a map into lines of text suitable for display.
 * This is useful for displaying lists of player stats, scores, etc.
 *
 * Example:
 * ```kotlin
 * val stocksText = playerStocks.toDisplayLines { (name, info) ->
 *     "$name: ${info.current}/${info.max}"
 * }
 * builder.setText("#StocksList", stocksText)
 * ```
 *
 * @param transform The transformation function for each entry
 * @return A newline-separated string of all entries
 */
public fun <K, V> Map<K, V>.toDisplayLines(
    transform: (Map.Entry<K, V>) -> String
): String = entries.joinToString("\n", transform = transform)

/**
 * Formats a list into lines of text suitable for display.
 *
 * Example:
 * ```kotlin
 * val playerList = onlinePlayers.toDisplayLines { it.name }
 * builder.setText("#PlayerList", playerList)
 * ```
 *
 * @param transform The transformation function for each item
 * @return A newline-separated string of all items
 */
public fun <T> List<T>.toDisplayLines(
    transform: (T) -> String
): String = joinToString("\n", transform = transform)
