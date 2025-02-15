package com.stslex.compiler_plugin_lib

import java.util.logging.Level
import java.util.logging.Logger

internal object DistinctChangeCache {

    private const val TAG = "KotlinCompilerDistinct"
    private val logger = Logger.getLogger(TAG)
    private val cache = mutableMapOf<String, Pair<List<Any?>, Any?>>()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <R> invoke(key: String, args: List<Any?>, body: () -> R): R {
        val entry = cache[key]
        logger.log(Level.INFO, "memorize key: $key, entry: $entry, args: $args")

        if (entry != null && entry.first == args) {
            return entry.second as R
        }

        val result = body()
        cache[key] = args to result
        return result
    }
}