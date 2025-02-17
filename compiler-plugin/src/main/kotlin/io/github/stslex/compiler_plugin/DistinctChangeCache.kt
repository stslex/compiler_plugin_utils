package io.github.stslex.compiler_plugin

import io.github.stslex.compiler_plugin.utils.RuntimeLogger

internal class DistinctChangeCache(
    private val config: DistinctChangeConfig
) {

    private val cache = mutableMapOf<String, Pair<List<Any?>, Any?>>()
    private val logger = RuntimeLogger.tag("DistinctChangeLogger")

    @Suppress("UNCHECKED_CAST")
    internal operator fun <R> invoke(
        key: String,
        args: List<Any?>,
        body: () -> R,
    ): R {
        val entry = cache[key]

        if (config.logging) {
            logger.i("key: $key, config:$config, entry: $entry, args: $args")
        }

        if (entry != null && entry.first == args) {
            if (config.logging) {
                logger.i("$key not change")
            }
            return entry.second as R
        }

        val result = body()
        cache[key] = args to result
        return result
    }
}