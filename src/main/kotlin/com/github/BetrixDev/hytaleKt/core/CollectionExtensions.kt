@file:JvmName("CollectionExtensions")

package com.github.BetrixDev.hytaleKt.core

/**
 * Returns a list containing at most [n] elements.
 * If the list has fewer than [n] elements, returns the list unchanged.
 *
 * @param n The maximum number of elements to take
 * @return A list with at most [n] elements
 */
public fun <T> List<T>.takeAtMost(n: Int): List<T> =
    if (size > n) subList(0, n) else this

/**
 * Returns a random element, or null if the collection is empty.
 *
 * @return A random element or null
 */
public fun <T> List<T>.randomOrNull(): T? =
    if (isEmpty()) null else random()

/**
 * Joins map entries into a multi-line string.
 *
 * @param transform Function to transform each entry into a string
 * @return A string with entries joined by newlines
 */
public fun <K, V> Map<K, V>.toLines(transform: (Map.Entry<K, V>) -> String): String =
    entries.joinToString("\n", transform = transform)

/**
 * Returns the first element matching the given [predicate], or null if none found.
 * This is an alias for [firstOrNull] for better discoverability.
 *
 * @param predicate The predicate to match
 * @return The first matching element or null
 */
public inline fun <T> Iterable<T>.findFirst(predicate: (T) -> Boolean): T? =
    firstOrNull(predicate)
