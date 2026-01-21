@file:JvmName("MessageExtensions")

package dev.betrix.hytale.kotlin.core

import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.command.system.CommandContext

/**
 * Sends a raw text message to the command sender.
 *
 * @param message The message text to send
 */
public fun CommandContext.msg(message: String) {
    sendMessage(Message.raw(message))
}

/**
 * Sends multiple raw text messages to the command sender.
 *
 * @param lines The message lines to send
 */
public fun CommandContext.msgLines(vararg lines: String) {
    lines.forEach { msg(it) }
}

/**
 * Sends a formatted message to the command sender.
 *
 * @param format The format string
 * @param args The format arguments
 */
public fun CommandContext.msgf(format: String, vararg args: Any) {
    msg(String.format(format, *args))
}
